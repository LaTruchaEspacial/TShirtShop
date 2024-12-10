package crm.t_shirtshop.admin_screens

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
import crm.t_shirtshop.comun_screens.SoporteActivity
import crm.t_shirtshop.ui.theme.TShirtShopTheme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                AdminScreen(
                    onManageUsersClick = {
                        startActivity(Intent(this, UsersAdminActivity::class.java))
                    },
                    onManageProductsClick = {
                        startActivity(Intent(this, ProductosAdminActivity::class.java))
                    },
                    onSupportClick = {
                        startActivity(Intent(this, SoporteActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun AdminScreen(
    onManageUsersClick: () -> Unit,
    onManageProductsClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onManageUsersClick) {
                Text(text = "Administrar usuarios")
            }
            Button(onClick = onManageProductsClick) {
                Text(text = "Administrar productos")
            }
            Button(onClick = onSupportClick) {
                Text(text = "Soporte")
            }
        }
    }
}
