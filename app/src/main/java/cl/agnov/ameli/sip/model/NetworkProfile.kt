package cl.agnov.ameli.sip.model

/**
 * Configuración de NAT/códec asociada a una red (p.ej. Wi-Fi de casa, datos
 * móviles, oficina), para cambiar rápido de una a otra sin reescribir todos
 * los campos de Configuración a mano.
 */
data class NetworkProfile(
    val id: Long = 0,
    val name: String,
    val stunEnabled: Boolean,
    val stunServer: String,
    val iceEnabled: Boolean,
    val turnEnabled: Boolean,
    val turnServer: String,
    val codecPriority: List<AudioCodec>,
)
