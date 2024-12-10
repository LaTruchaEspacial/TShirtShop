package crm.t_shirtshop.comun_screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import crm.t_shirtshop.dataClass.Mensaje
import crm.t_shirtshop.ui.theme.TShirtShopTheme

class SoporteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtener la actividad previa desde el Intent
        val previousActivity = intent.getStringExtra("previousActivity") ?: ""

        setContent {
            TShirtShopTheme {
                SoporteScreen(previousActivity) {
                    // Lógica para regresar a la actividad previa dinámicamente
                    if (previousActivity.isNotBlank()) {
                        try {
                            val previousClass = Class.forName(previousActivity)
                            val intent = Intent(this, previousClass)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        } catch (e: ClassNotFoundException) {
                            Toast.makeText(this, "Error: actividad previa no encontrada", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        finish() // Si no se especifica actividad previa, simplemente finalizamos
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoporteScreen(previousActivity: String, onBackClick: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var nuevoMensaje by remember { mutableStateOf("") }
    var mensajes = remember { mutableStateListOf<Mensaje>() } // Usamos mutableStateListOf para la lista
    val context = LocalContext.current

    // Escuchar mensajes en tiempo real desde Firestore
    LaunchedEffect(Unit) {
        db.collection("soporte")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING) // Ordenar por timestamp
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Toast.makeText(context, "Error al cargar los mensajes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val loadedMessages = mutableListOf<Mensaje>()

                    // Procesar los mensajes para obtener el nombre y rol
                    snapshot.documents.forEach { document ->
                        val userId = document.getString("userId") ?: ""
                        val texto = document.getString("texto") ?: ""
                        val timestamp = document.getLong("timestamp") ?: 0L // Asegúrate de obtener el timestamp

                        // Obtener nombre y rol del usuario de Firestore
                        obtenerNombreYRoleUsuario(userId, db) { userName, role ->
                            loadedMessages.add(Mensaje(userId, texto, role, userName, timestamp))

                            // Cuando todos los mensajes estén cargados, actualizamos el estado de la lista
                            if (loadedMessages.size == snapshot.size()) {
                                mensajes.clear()
                                mensajes.addAll(loadedMessages.sortedBy { it.timestamp })
                            }
                        }
                    }
                } else {
                    mensajes.clear() // Limpiar la lista si no hay mensajes
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat de Soporte") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Lista de mensajes
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                items(mensajes) { mensaje ->
                    MessageItem(mensaje = mensaje, isAdmin = mensaje.role == "admin")
                }
            }

            // Campo de entrada y botón de enviar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = nuevoMensaje,
                    onValueChange = { nuevoMensaje = it },
                    label = { Text("Escribe tu mensaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (nuevoMensaje.isNotBlank() && user != null) {
                            val userId = user.uid

                            // Consultamos el rol del usuario antes de enviar el mensaje
                            obtenerNombreYRoleUsuario(userId, db) { userName, role ->
                                val mensaje = mapOf(
                                    "userId" to userId,
                                    "texto" to nuevoMensaje,
                                    "role" to role,
                                    "timestamp" to System.currentTimeMillis() // Guardamos el timestamp actual
                                )

                                db.collection("soporte").add(mensaje)
                                    .addOnSuccessListener {
                                        nuevoMensaje = ""
                                        Toast.makeText(context, "Mensaje enviado", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(context, "Error al enviar el mensaje: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

@Composable
fun MessageItem(mensaje: Mensaje, isAdmin: Boolean) {
    val alignment = if (isAdmin) Alignment.Start else Alignment.End
    val backgroundColor = if (isAdmin) Color.Gray else Color(0xFFFFD700)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isAdmin) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                .padding(12.dp)
                .widthIn(max = 250.dp), // Limita el ancho máximo del mensaje
            horizontalAlignment = Alignment.Start
        ) {
            // Nombre del usuario (opcional)
            Text(
                text = mensaje.userName,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAdmin) Color.White else Color.Black
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Contenido del mensaje
            Text(
                text = mensaje.texto,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isAdmin) Color.White else Color.Black
                )
            )
        }
    }
}

fun obtenerNombreYRoleUsuario(userId: String, db: FirebaseFirestore, onComplete: (String, String) -> Unit) {
    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val userName = document.getString("nombre") ?: "Desconocido"
                val role = document.getString("role") ?: "user" // Obtenemos el role
                onComplete(userName, role)
            } else {
                onComplete("Desconocido", "user")
            }
        }
        .addOnFailureListener {
            onComplete("Desconocido", "user")
        }
}
