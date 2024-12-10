package crm.t_shirtshop.auth

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import crm.t_shirtshop.admin_screens.AdminActivity
import crm.t_shirtshop.client_screens.HomeActivity
import crm.t_shirtshop.dataClass.User

class Auth(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Método para registrar un usuario con email, contraseña, nombre y apellido
    fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Crear objeto User
                        val user = User(
                            userId = userId,
                            email = email,
                            nombre = nombre,
                            apellido = apellido,
                            role = "user" // Valor predeterminado para el rol
                        )
                        // Guardar el usuario en Firestore
                        firestore.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                onResult(true, null) // Registro exitoso y datos guardados
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Error al guardar los datos: ${e.message}")
                            }
                    } else {
                        onResult(false, "No se pudo obtener el ID del usuario")
                    }
                } else {
                    onResult(false, task.exception?.message) // Error en el registro
                }
            }
    }

    // Método para iniciar sesión con email y contraseña
    fun login(
        context: Context,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Consultar el campo "role" del usuario en Firestore
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    // Mapear el documento a un objeto User
                                    val user = document.toObject(User::class.java)
                                    if (user != null) {
                                        // Redirigir según el rol
                                        val intent = if (user.role == "admin") {
                                            Intent(context, AdminActivity::class.java)
                                        } else {
                                            Intent(context, HomeActivity::class.java)
                                        }
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        onResult(true, null)
                                    } else {
                                        onResult(false, "Error al mapear los datos del usuario")
                                    }
                                } else {
                                    onResult(false, "Usuario no encontrado")
                                }
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Error al consultar el rol: ${e.message}")
                            }
                    } else {
                        onResult(false, "No se pudo obtener el ID del usuario")
                    }
                } else {
                    onResult(false, task.exception?.message) // Error en el inicio de sesión
                }
            }
    }

    // Método para cargar los datos del usuario actual desde Firestore
    fun loadUserData(onResult: (User?, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            onResult(user, null) // Datos obtenidos exitosamente
                        } else {
                            onResult(null, "Error al mapear los datos del usuario")
                        }
                    } else {
                        onResult(null, "Usuario no encontrado")
                    }
                }
                .addOnFailureListener { e ->
                    onResult(null, "Error al cargar los datos: ${e.message}")
                }
        } else {
            onResult(null, "Usuario no autenticado")
        }
    }

    // Método para cerrar sesión
    fun logout() {
        auth.signOut() // Desconecta al usuario de Firebase
    }
}
