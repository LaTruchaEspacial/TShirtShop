package crm.t_shirtshop.dataClass

data class Mensaje(
    val userId: String = "",
    val texto: String = "",
    val role: String = "",
    val userName: String = ""  // Agregamos el nombre del usuario aquí
)
