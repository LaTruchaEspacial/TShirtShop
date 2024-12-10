package crm.t_shirtshop.dataClass

data class Mensaje(
    val userId: String = "",
    val texto: String = "",
    val role: String = "",
    val userName: String = "",
    val timestamp: Long = 0L  // Campo para el timestamp
)
