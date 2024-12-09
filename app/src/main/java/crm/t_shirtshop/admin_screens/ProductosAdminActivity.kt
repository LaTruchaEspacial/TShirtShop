package crm.t_shirtshop.admin_screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import crm.t_shirtshop.dataClass.Camiseta
import crm.t_shirtshop.ui.theme.TShirtShopTheme
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

class ProductosAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                ProductosAdminScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosAdminScreen() {
    var camisetaList by remember { mutableStateOf<List<Camiseta>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCamiseta by remember { mutableStateOf<Camiseta?>(null) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Filtrar camisetas por nombre
    val filteredCamisetas = camisetaList.filter {
        it.nombre.contains(searchQuery, ignoreCase = true)
    }

    // Obtener camisetas de Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("camisetas").get()
            .addOnSuccessListener { result ->
                camisetaList = result.map { document ->
                    Camiseta(
                        camisetaId = document.id,
                        nombre = document.getString("nombre") ?: "",
                        precio = document.getDouble("precio") ?: 0.0,
                        url = document.getString("url") ?: "",
                        cantidadDisponible = document.getLong("cantidad")?.toInt() ?: 0
                    )
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Productos Admin",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                ),
                actions = {
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menú", tint = Color.White)
                    }
                }
            )
        },
        content = { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                Column {
                    // Buscador de camisetas
                    SearchBar(searchQuery) { searchQuery = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de camisetas
                    CamisetaList(camisetas = filteredCamisetas, onEdit = { camiseta ->
                        selectedCamiseta = camiseta
                    })

                    // Mostrar el formulario de edición si se ha seleccionado una camiseta
                    selectedCamiseta?.let { camiseta ->
                        EditCamisetaForm(camiseta = camiseta, onDismiss = {
                            selectedCamiseta = null
                        }, onUpdate = { updatedCamiseta ->
                            // Actualizar camiseta en la lista de UI
                            camisetaList = camisetaList.map {
                                if (it.camisetaId == updatedCamiseta.camisetaId) updatedCamiseta else it
                            }
                            selectedCamiseta = null
                        })
                    }
                }
            }
        }
    )

    // Menú lateral desplegable
    if (isMenuExpanded) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(Color(0xFF1976D2))
                .padding(top = 56.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                val context = LocalContext.current

                // Opción "Usuarios"
                MenuItem(text = "Usuarios") {
                    isMenuExpanded = false
                    val intent = Intent(context, UsersAdminActivity::class.java)
                    context.startActivity(intent)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Opción "Productos"
                MenuItem(text = "Productos") {
                    isMenuExpanded = false
                    val intent = Intent(context, ProductosAdminActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }
}



@Composable
fun CamisetaList(camisetas: List<Camiseta>, onEdit: (Camiseta) -> Unit) {
    LazyColumn {
        items(camisetas.size) { index ->
            CamisetaListItem(camiseta = camisetas[index], onEdit = onEdit)
        }
    }
}

@Composable
fun CamisetaListItem(camiseta: Camiseta, onEdit: (Camiseta) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit(camiseta) }
            .alpha(if (camiseta.cantidadDisponible == 0) 0.3f else 1f) // Si está agotado, lo vuelve transparente
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${camiseta.nombre}")
            Text(text = "Precio: \$${camiseta.precio}")
            Text(text = "Cantidad disponible: ${camiseta.cantidadDisponible}")
            if (camiseta.cantidadDisponible == 0) {
                Text(text = "AGOTADO", color = Color.Red)
            }
        }
    }
}

@Composable
fun EditCamisetaForm(camiseta: Camiseta, onDismiss: () -> Unit, onUpdate: (Camiseta) -> Unit) {
    var cantidad by remember { mutableStateOf(camiseta.cantidadDisponible) }

    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        // Campo cantidad
        TextField(
            value = cantidad.toString(),
            onValueChange = { cantidad = it.toIntOrNull() ?: cantidad },
            label = { Text("Cantidad disponible") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Botones de acción
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    // Actualizar camiseta en Firestore
                    val camisetaMap = mapOf("cantidadDisponible" to cantidad)
                    firestore.collection("camisetas").document(camiseta.camisetaId)
                        .update(camisetaMap)
                        .addOnSuccessListener {
                            val updatedCamiseta = camiseta.copy(cantidadDisponible = cantidad)
                            onUpdate(updatedCamiseta) // Actualiza la camiseta en la lista
                            onDismiss() // Cierra el formulario
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(context, "Error al actualizar: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Actualizar", color = Color.White)
            }

            Button(
                onClick = { onDismiss() }, // Cerrar el formulario sin guardar cambios
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf44336))
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    }
}
