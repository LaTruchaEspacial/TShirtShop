// HomeActivity.kt
package crm.t_shirtshop.client_screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Asegúrate de este import
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import crm.t_shirtshop.ui.theme.TShirtShopTheme
import crm.t_shirtshop.R
import crm.t_shirtshop.comun_screens.ProfileActivity

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
// HomeActivity.kt (dentro de la función HomeScreen)
@Composable
fun HomeScreen() {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Header
            TopAppBar(
                title = { Text(text = "Tienda Camisetas", fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // icono de 3 rayas
                            contentDescription = "Menú",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier.fillMaxWidth()
            )

            // Content (después del header)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Bienvenido a la tienda de camisetas", fontSize = 24.sp)
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
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }
                }
            }
        }
    }
}


@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    ClickableText(
        text = androidx.compose.ui.text.AnnotatedString(text),
        onClick = { onClick() },
        modifier = Modifier.fillMaxWidth(),
        style = androidx.compose.ui.text.TextStyle(
            color = Color.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Left
        )
    )
}
