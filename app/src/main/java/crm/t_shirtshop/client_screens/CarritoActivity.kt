package crm.t_shirtshop.client_screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import crm.t_shirtshop.comun_screens.ProfileActivity
import crm.t_shirtshop.dataClass.Camiseta
import crm.t_shirtshop.dataClass.User
import crm.t_shirtshop.ui.theme.TShirtShopTheme
import crm.t_shirtshop.R
import crm.t_shirtshop.comun_screens.SoporteActivity


class CarritoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TShirtShopTheme {
                CarritoScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen() {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser // Obtener el usuario autenticado

    var carritoItems by remember { mutableStateOf<List<Pair<Camiseta, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar los artículos del carrito
    LaunchedEffect(Unit) {
        if (user != null) {
            db.collection("carrito")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { result ->
                    val items = mutableListOf<Pair<Camiseta, Int>>()
                    for (document in result) {
                        val camisetaId = document.getString("camisetaId") ?: ""
                        val cantidad = document.getLong("cantidad")?.toInt() ?: 0
                        if (camisetaId.isNotEmpty()) {
                            db.collection("camisetas").document(camisetaId).get()
                                .addOnSuccessListener { camisetaDoc ->
                                    val nombre = camisetaDoc.getString("nombre") ?: ""
                                    val precio = camisetaDoc.getDouble("precio") ?: 0.0
                                    val url = camisetaDoc.getString("url") ?: ""
                                    val cantidadDisponible = camisetaDoc.getDouble("cantidad")?.toInt() ?: 0
                                    val camiseta = Camiseta(camisetaId, nombre, precio, url, cantidadDisponible)
                                    items.add(Pair(camiseta, cantidad))
                                    if (items.size == result.size()) {
                                        carritoItems = items
                                        isLoading = false
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al obtener camiseta", Toast.LENGTH_SHORT).show()
                                    isLoading = false
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al cargar el carrito", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        } else {
            Toast.makeText(context, "Por favor, inicie sesión.", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con el botón para abrir el menú
            TopAppBar(
                title = { Text(text = "Tu Carrito", fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { isMenuExpanded = !isMenuExpanded }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menú", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier.fillMaxWidth()
            )

            // Mostrar el precio total arriba de las camisetas
            val totalPrecio = carritoItems.sumOf { (camiseta, cantidad) ->
                camiseta.precio * cantidad // Multiplicamos el precio por la cantidad en el carrito
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Precio Total: ", fontSize = 18.sp, color = Color.Black)
                Text(text = "${"%.2f".format(totalPrecio)} €", fontSize = 18.sp, color = Color.Black)
            }

            // Botón de compra
            Button(
                onClick = {
                    if (carritoItems.isNotEmpty()) {
                        carritoItems.forEach { (camiseta, cantidad) ->
                            val camisetaRef = db.collection("camisetas").document(camiseta.camisetaId)

                            // Actualizar la cantidad disponible
                            camisetaRef.get()
                                .addOnSuccessListener { camisetaDoc ->
                                    val cantidadDisponibleActual = camisetaDoc.getDouble("cantidad")?.toInt() ?: 0
                                    val nuevaCantidadDisponible = cantidadDisponibleActual - cantidad

                                    if (nuevaCantidadDisponible >= 0) {
                                        camisetaRef.update("cantidad", nuevaCantidadDisponible)
                                            .addOnSuccessListener {
                                                // Eliminar el artículo del carrito
                                                db.collection("carrito")
                                                    .whereEqualTo("userId", user?.uid)
                                                    .whereEqualTo("camisetaId", camiseta.camisetaId)
                                                    .get()
                                                    .addOnSuccessListener { querySnapshot ->
                                                        for (document in querySnapshot) {
                                                            document.reference.delete()
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(context, "Error al limpiar el carrito", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Error al actualizar cantidades", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Cantidad insuficiente para ${camiseta.nombre}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Error al acceder a los datos de la camiseta", Toast.LENGTH_SHORT).show()
                                }
                        }

                        // Limpiar la lista local del carrito y mostrar mensaje de éxito
                        carritoItems = emptyList()
                        Toast.makeText(context, "Compra realizada con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color(0xFFFFC107)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text(text = "Comprar", fontSize = 18.sp, color = Color.White)
            }

            // Cargar lista de camisetas
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
                    items(carritoItems) { (camiseta, cantidad) ->
                        CarritoItem(
                            camiseta = camiseta,
                            cantidad = cantidad,
                            onAddToCart = { camisetaId, cantidad ->
                                actualizarCantidadEnCarrito(context, camisetaId, cantidad, true, carritoItems) { newItems ->
                                    carritoItems = newItems // Actualiza el estado local de la lista de artículos
                                }
                            },
                            onRemoveFromCart = { camisetaId, cantidad ->
                                actualizarCantidadEnCarrito(context, camisetaId, cantidad, false, carritoItems) { newItems ->
                                    carritoItems = newItems // Actualiza el estado local de la lista de artículos
                                }
                            }

                        )
                    }
                }
            }

            // Spacer para empujar el contenido hacia abajo
            Spacer(modifier = Modifier.weight(1f))
        }

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

fun actualizarCantidadEnCarrito(
    context: Context,
    camisetaId: String,
    cantidad: Int,
    esAumento: Boolean,
    carritoItems: List<Pair<Camiseta, Int>>,
    onUpdated: (List<Pair<Camiseta, Int>>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user != null) {
        val carritoRef = db.collection("carrito")
            .whereEqualTo("userId", user.uid)
            .whereEqualTo("camisetaId", camisetaId)

        carritoRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Si no se encuentra el artículo en el carrito, lo agregamos
                    val carritoItem = hashMapOf(
                        "userId" to user.uid,
                        "camisetaId" to camisetaId,
                        "cantidad" to cantidad
                    )
                    db.collection("carrito").add(carritoItem)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Camiseta agregada al carrito", Toast.LENGTH_SHORT).show()
                            // Actualizar carrito con la cantidad correcta
                            onUpdated(carritoItems + Pair(Camiseta(camisetaId, "", 0.0, "", 0), cantidad))
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al agregar camiseta al carrito", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Si el artículo ya está en el carrito, actualizamos la cantidad
                    val currentItem = querySnapshot.documents.first()
                    val currentCantidad = currentItem.getLong("cantidad")?.toInt() ?: 0
                    val nuevaCantidad = if (esAumento) currentCantidad + 1 else currentCantidad - 1

                    if (nuevaCantidad > 0) {
                        currentItem.reference.update("cantidad", nuevaCantidad)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Cantidad actualizada en el carrito", Toast.LENGTH_SHORT).show()

                                // Actualizamos el estado local de la lista del carrito con la nueva cantidad
                                val updatedItems = carritoItems.map {
                                    if (it.first.camisetaId == camisetaId) {
                                        Pair(it.first, nuevaCantidad)
                                    } else {
                                        it
                                    }
                                }
                                onUpdated(updatedItems)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al actualizar la cantidad", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Si la cantidad llega a 0, eliminamos el artículo del carrito
                        currentItem.reference.delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Artículo eliminado del carrito", Toast.LENGTH_SHORT).show()
                                onUpdated(carritoItems.filterNot { it.first.camisetaId == camisetaId })
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar artículo del carrito", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al acceder al carrito", Toast.LENGTH_SHORT).show()
            }
    } else {
        Toast.makeText(context, "Por favor, inicie sesión para modificar el carrito", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun CarritoItem(
    camiseta: Camiseta,
    cantidad: Int,
    onAddToCart: (String, Int) -> Unit,
    onRemoveFromCart: (String, Int) -> Unit
) {
    var cantidadInCart by remember { mutableStateOf(cantidad) }
    // Convertir la URL en un nombre válido para un recurso drawable
    val drawableName = urlToDrawableName(camiseta.url)
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Mostrar la imagen desde los recursos locales
            if (resourceId != 0) {
                // Si la imagen existe en drawable
                Image(
                    painter = painterResource(id = resourceId), // Usamos el ID de la imagen en drawable
                    contentDescription = "Imagen de la camiseta",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Ajusta la altura de la imagen
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop // Escala la imagen para que ocupe todo el espacio disponible
                )
            } else {
                // Si no encontramos la imagen, mostramos una imagen por defecto
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Imagen por defecto en drawable
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (cantidadInCart > 0) {
                        cantidadInCart -= 1
                        onRemoveFromCart(camiseta.camisetaId, cantidadInCart)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Disminuir cantidad"
                    )
                }

                Text(text = "$cantidadInCart", fontSize = 18.sp, color = Color.Black)

                IconButton(onClick = {
                    if (cantidadInCart < camiseta.cantidadDisponible) {
                        cantidadInCart += 1
                        onAddToCart(camiseta.camisetaId, cantidadInCart)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Aumentar cantidad"
                    )
                }
            }
        }
    }
}