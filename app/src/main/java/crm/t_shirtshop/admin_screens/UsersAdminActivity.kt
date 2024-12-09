package crm.t_shirtshop.admin_screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import crm.t_shirtshop.dataClass.User
import crm.t_shirtshop.ui.theme.TShirtShopTheme
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextPainter
import androidx.compose.ui.unit.sp

class UsersAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                UsersAdminScreen()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersAdminScreen() {
    // Estado para los usuarios
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Filtrar usuarios por nombre
    val filteredUsers = userList.filter {
        it.nombre.contains(searchQuery, ignoreCase = true)
    }

    // Obtener usuarios de Firestore
    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                userList = result.map { document ->
                    User(
                        userId = document.id,
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        email = document.getString("email") ?: "",
                        role = document.getString("role") ?: ""
                    )
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Administrador",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2) // Usando containerColor en lugar de backgroundColor
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
                    // Buscador de usuarios
                    SearchBar(searchQuery) { searchQuery = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Lista de usuarios
                    UserList(users = filteredUsers, onEdit = { user ->
                        selectedUser = user
                    })

                    // Mostrar el formulario de edición si se ha seleccionado un usuario
                    selectedUser?.let { user ->
                        EditUserForm(user = user, onDismiss = {
                            selectedUser = null // Cierra el formulario al actualizar
                        }, onUpdate = { updatedUser ->
                            // Actualizar la lista de usuarios en la UI sin salir de la pantalla
                            userList = userList.map {
                                if (it.userId == updatedUser.userId) updatedUser else it
                            }
                            selectedUser = null // Cerrar el formulario
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
fun MenuItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 20.sp,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SearchBar(searchQuery: String, onQueryChanged: (String) -> Unit) {
    TextField(
        value = searchQuery,
        onValueChange = onQueryChanged,
        label = { Text("Buscar Usuario") },
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        singleLine = true
    )
}

@Composable
fun UserList(users: List<User>, onEdit: (User) -> Unit) {
    LazyColumn {
        items(users.size) { index ->
            UserListItem(user = users[index], onEdit = onEdit)
        }
    }
}

@Composable
fun UserListItem(user: User, onEdit: (User) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onEdit(user) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${user.nombre} ${user.apellido}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${user.email}")
            Text(text = "Rol: ${user.role}", color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { /* Eliminar usuario */ }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Usuario")
                }
            }
        }
    }
}

@Composable
fun EditUserForm(user: User, onDismiss: () -> Unit, onUpdate: (User) -> Unit) {
    var nombre by remember { mutableStateOf(user.nombre) }
    var apellido by remember { mutableStateOf(user.apellido) }
    var role by remember { mutableStateOf(user.role) }

    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current // Obtener el contexto correctamente aquí

    Column(modifier = Modifier.padding(16.dp)) {
        // Campo Nombre
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Campo Apellido
        TextField(
            value = apellido,
            onValueChange = { apellido = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Campo Rol
        TextField(
            value = role,
            onValueChange = { role = it },
            label = { Text("Rol") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Botones de acción
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Button(
                onClick = {
                    // Actualizar usuario en Firestore
                    val userMap = mapOf(
                        "nombre" to nombre,
                        "apellido" to apellido,
                        "role" to role
                    )
                    firestore.collection("users").document(user.userId)
                        .update(userMap)
                        .addOnSuccessListener {
                            val updatedUser = user.copy(nombre = nombre, apellido = apellido, role = role)
                            onUpdate(updatedUser) // Actualiza el usuario en la lista
                            onDismiss() // Cierra el formulario
                        }
                        .addOnFailureListener { exception ->
                            // Usar el contexto correctamente para el Toast
                            Toast.makeText(context, "Error al actualizar: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Botón verde
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Actualizar", color = Color.White)
            }

            Button(
                onClick = { onDismiss() }, // Cerrar el formulario sin guardar cambios
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf44336)) // Botón rojo
            ) {
                Text("Cancelar", color = Color.White)
            }
        }
    }
}
