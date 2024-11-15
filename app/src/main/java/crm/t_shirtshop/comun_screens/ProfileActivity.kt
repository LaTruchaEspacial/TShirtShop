// ProfileActivity.kt
package crm.t_shirtshop.comun_screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import crm.t_shirtshop.R
import crm.t_shirtshop.auth.Auth
import crm.t_shirtshop.client_screens.HomeActivity
import crm.t_shirtshop.client_screens.MenuItem
import crm.t_shirtshop.ui.theme.TShirtShopTheme

class ProfileActivity : ComponentActivity() {

    private val auth = Auth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                ProfileScreen(auth)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(auth: Auth) {
    val context = LocalContext.current
    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        auth.loadUserData { data, error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            } else {
                userData = data
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column {
            // Header con botón de menú
            TopAppBar(
                title = { Text(text = "Mi cuenta", fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // icono de 3 rayas
                            contentDescription = "Menú",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Yellow),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido principal: foto de perfil y tarjeta con datos del usuario
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxSize().padding(top = 16.dp)
            ) {
                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.Gray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Foto", color = Color.White, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Datos del usuario", fontSize = 18.sp)

                // Tarjeta con datos del usuario
                userData?.let { data ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Nombre: ${data["nombre"] ?: "N/A"}", fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Apellido: ${data["apellido"] ?: "N/A"}", fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Email: ${data["email"] ?: "N/A"}", fontSize = 16.sp)
                        }
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
                    MenuItem(text = "Promociones") { isMenuExpanded = false }
                    Spacer(modifier = Modifier.height(32.dp))
                    MenuItem(text = "Tu compra") { isMenuExpanded = false }
                    Spacer(modifier = Modifier.height(32.dp))
                    MenuItem(text = "Mi cuenta") {
                        isMenuExpanded = false
                        // Aquí podríamos recargar la pantalla actual o realizar alguna acción adicional
                    }
                }
            }
        }
    }
@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Maneja el clic
            .padding(16.dp), // Añadimos espacio para que no esté tan pegado
        color = Color.Black,
        fontSize = 18.sp,
        textAlign = TextAlign.Left
    )
}}
