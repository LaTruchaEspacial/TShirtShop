package crm.t_shirtshop.client_screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import crm.t_shirtshop.R
import crm.t_shirtshop.comun_screens.ProfileActivity
import crm.t_shirtshop.comun_screens.SoporteActivity
import crm.t_shirtshop.ui.theme.TShirtShopTheme
import crm.t_shirtshop.dataClass.Camiseta

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var camisetas by remember { mutableStateOf<List<Camiseta>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Obtener el usuario actual desde Firebase Auth
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser // Aquí obtienes el usuario autenticado

    // Cargar camisetas desde Firebase
    LaunchedEffect(Unit) {
        db.collection("camisetas").get()
            .addOnSuccessListener { result ->
                camisetas = result.map { document ->
                    val id = document.id // Firestore proporciona el ID del documento
                    val nombre = document.getString("nombre") ?: ""
                    val precio = document.getDouble("precio") ?: 0.0
                    val url = document.getString("url") ?: ""  // Aquí sigue siendo necesario tener la URL
                    val cantidadDisponible = document.getDouble("cantidad") ?: 0.0
                    Camiseta(id, nombre, precio, url, cantidadDisponible.toInt())
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Error al cargar camisetas: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                isLoading = false
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Header
            TopAppBar(
                title = { Text(text = "Tienda Camisetas", fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(
                            painter = painterResource(id = R.drawable.iconomenu),
                            contentDescription = "Menú",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier.fillMaxWidth()
            )

            // Lista de camisetas
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Yellow)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(camisetas) { camiseta ->
                        CamisetaItem(
                            camiseta = camiseta,
                            onAddToCart = { camisetaId, cantidadDisponible ->
                                // Lógica para añadir al carrito
                                if (user != null) {
                                    val carritoItem = mapOf(
                                        "userId" to user.uid, // Utilizar el UID del usuario actual
                                        "camisetaId" to camisetaId, // Pasamos el ID de la camiseta
                                        "cantidad" to 0  // Pasamos la cantidad inicial (1)
                                    )
                                    db.collection("carrito").add(carritoItem)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Añadido al carrito",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                context,
                                                "Error: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Por favor, inicie sesión.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onBuy = { camisetaId, cantidadComprada, cantidadDisponible ->
                                // Comprueba si la cantidad disponible es suficiente para comprar
                                if (cantidadComprada <= cantidadDisponible) {
                                    db.collection("camisetas").document(camisetaId)
                                        .update("cantidad", cantidadDisponible - cantidadComprada)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                context,
                                                "Compra realizada con éxito",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(
                                                context,
                                                "Error: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    Toast.makeText(context, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        // Menú lateral desplegable
        if (isMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .background(Color(0xFFFFC107))
                    .align(Alignment.TopStart)
                    .padding(top = 56.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    MenuItem(text = "Tienda") {
                        isMenuExpanded = false
                        context.startActivity(Intent(context, HomeActivity::class.java))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    MenuItem(text = "Tu compra") {
                        isMenuExpanded = false
                        context.startActivity(Intent(context, CarritoActivity::class.java))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    MenuItem(text = "Mi cuenta") {
                        isMenuExpanded = false
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    // Nueva opción de Soporte
                    MenuItem(text = "Soporte") {
                        isMenuExpanded = false
                        context.startActivity(Intent(context, SoporteActivity::class.java)) // Asegúrate de crear la SoporteActivity
                    }
                }
            }
        }

    }
}

// Función para convertir la URL en un nombre de archivo válido para drawable
fun urlToDrawableName(url: String): String {
    return url.substringAfterLast("/").substringBeforeLast(".").replace("-", "_")
        .replace(".", "_")
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        color = Color.Black,
        fontSize = 18.sp,
        textAlign = TextAlign.Left
    )
}
@Composable
fun CamisetaItem(
    camiseta: Camiseta,
    onAddToCart: (String, Int) -> Unit,  // Recibe el ID de la camiseta y la cantidad
    onBuy: (String, Int, Int) -> Unit    // Función para comprar (ID de la camiseta, cantidad comprada, cantidad disponible)
) {
    val drawableName = urlToDrawableName(camiseta.url)
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    var isOutOfStock by remember { mutableStateOf(camiseta.cantidadDisponible == 0) }

    LaunchedEffect(camiseta.cantidadDisponible) {
        isOutOfStock = camiseta.cantidadDisponible == 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Mostrar la imagen desde los recursos locales
                if (resourceId != 0) {
                    Image(
                        painter = painterResource(id = resourceId),
                        contentDescription = "Imagen de la camiseta",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Imagen por defecto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(text = "Nombre: ${camiseta.nombre}", fontSize = 20.sp, color = Color.Black)
                Text(text = "Precio: ${camiseta.precio} €", fontSize = 18.sp, color = Color.Gray)
                Text(
                    text = "Cantidad disponible: ${camiseta.cantidadDisponible}",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                if (isOutOfStock) {
                    // Mostrar "Agotado"
                    Text(
                        text = "AGOTADO",
                        color = Color.Red,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    // Deshabilitar botones y poner capa translúcida
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Usa fillMaxSize en lugar de matchParentSize
                            .background(Color.Gray.copy(alpha = 0.5f)) // Capa translúcida
                            .align(Alignment.CenterHorizontally) // Alinea el contenido dentro del Box
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isOutOfStock) {
                                onAddToCart(camiseta.camisetaId, 0)  // Añadir al carrito con cantidad 1
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        enabled = !isOutOfStock // Deshabilitar el botón si está agotada
                    ) {
                        Text("Add Carrito", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            // Verifica si la cantidad disponible es suficiente para la compra
                            if (!isOutOfStock && camiseta.cantidadDisponible > 0) {
                                onBuy(camiseta.camisetaId, 1, camiseta.cantidadDisponible) // Comprar 1 artículo
                            } else {
                                Toast.makeText(context, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        enabled = !isOutOfStock // Deshabilitar el botón si está agotada
                    ) {
                        Text("Comprar", color = Color.White)
                    }
                }
            }
        }
    }
}