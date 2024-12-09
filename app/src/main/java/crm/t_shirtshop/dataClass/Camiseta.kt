package crm.t_shirtshop.dataClass

data class Camiseta(
    val camisetaId: String,   // El ID único de la camiseta
    val nombre: String,       // Nombre de la camiseta
    val precio: Double,       // Precio de la camiseta
    val url: String,
    val cantidadDisponible :Int
)
