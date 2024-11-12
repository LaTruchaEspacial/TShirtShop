package crm.t_shirtshop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import crm.t_shirtshop.comun_screens.LoginActivity
import crm.t_shirtshop.comun_screens.RegisterActivity
import crm.t_shirtshop.ui.theme.TShirtShopTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                MainScreen(
                    onLoginClick = { startActivity(Intent(this, LoginActivity::class.java)) },
                    onRegisterClick = { startActivity(Intent(this, RegisterActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
            Text("Registrarse")
        }
    }
}
