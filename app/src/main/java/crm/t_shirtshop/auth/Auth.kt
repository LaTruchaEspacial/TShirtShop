package crm.t_shirtshop.auth


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Auth(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Método para registrar un usuario con email y contraseña
    fun register(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Guardar los datos del usuario en Firestore
                        val user = hashMapOf(
                            "email" to email,
                            "userId" to userId
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
}
