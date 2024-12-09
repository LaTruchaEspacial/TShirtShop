package crm.t_shirtshop.dataClass

data class User(
    val userId: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val role: String = "user"
)