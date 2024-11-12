package crm.t_shirtshop.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Auth(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Método para registrar un usuario con email, contraseña, nombre y apellido
    fun register(email: String, password: String, nombre: String, apellido: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Guardar los datos del usuario en Firestore
                        val user = hashMapOf(
                            "email" to email,
                            "userId" to userId,
                            "nombre" to nombre,
                            "apellido" to apellido
                        )
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
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null) // Inicio de sesión exitoso
                } else {
                    onResult(false, task.exception?.message) // Error en el inicio de sesión
                }
            }
    }

    fun loadUserData(onResult: (Map<String, Any>?, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        onResult(document.data, null) // Datos obtenidos exitosamente
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
}
