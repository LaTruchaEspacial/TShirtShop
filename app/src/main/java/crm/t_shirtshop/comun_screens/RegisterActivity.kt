package crm.t_shirtshop.comun_screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import crm.t_shirtshop.auth.Auth
import crm.t_shirtshop.client_screens.HomeActivity
import crm.t_shirtshop.ui.theme.TShirtShopTheme
import crm.t_shirtshop.R
import crm.t_shirtshop.comun_screens.LoginActivity

class RegisterActivity : ComponentActivity() {

    private val auth = Auth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                RegisterScreen(
                    onRegister = { email, password, nombre, apellido ->
                        auth.register(email, password, nombre, apellido) { success, message ->
                            if (success) {
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, message ?: "Error al registrarse", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onLoginClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                )
            }
        }
    }
}
@Composable
fun RegisterScreen(onRegister: (String, String, String, String) -> Unit, onLoginClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var lastNameError by remember { mutableStateOf<String?>(null) }

    val isFormValid = emailError == null && passwordError == null && nameError == null &&
            lastNameError == null && email.isNotBlank() && password.isNotBlank() &&
            nombre.isNotBlank() && apellido.isNotBlank()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo4),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "REGÍSTRATE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            nameError = if (nombre.any { c -> !c.isLetter() && c != ' ' }) {
                                "Nombre no válido"
                            } else null
                        },
                        label = { Text("Nombre") },
                        isError = nameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    nameError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = apellido,
                        onValueChange = {
                            apellido = it
                            lastNameError = if (apellido.any { c -> !c.isLetter() && c != ' ' }) {
                                "Apellido no válido"
                            } else null
                        },
                        label = { Text("Apellido") },
                        isError = lastNameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    lastNameError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                "Correo electrónico inválido"
                            } else null
                        },
                        label = { Text("Correo Electrónico") },
                        isError = emailError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    emailError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = when {
                                password.contains(" ") -> "La contraseña no puede contener espacios"
                                password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                                else -> null
                            }
                        },
                        label = { Text("Contraseña") },
                        isError = passwordError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    passwordError?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onRegister(email, password, nombre, apellido) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Text(
                            text = "Registrarse",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(13.dp))

                    Text(
                        text = "¿Tienes cuenta? Inicia sesión",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onLoginClick() }
                    )
                }
            }
        }
    }
}
