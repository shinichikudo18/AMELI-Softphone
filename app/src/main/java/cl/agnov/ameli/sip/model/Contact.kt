package cl.agnov.ameli.sip.model

data class Contact(
    val id: Long = 0,
    val name: String,
    val sipAddress: String,
    val isFavorite: Boolean = false,
)
