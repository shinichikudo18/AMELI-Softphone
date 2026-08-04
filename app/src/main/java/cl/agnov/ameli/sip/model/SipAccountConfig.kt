package cl.agnov.ameli.sip.model

enum class SipTransport { UDP, TCP, TLS }

/** Códecs de audio soportados que el usuario puede priorizar/desactivar. */
enum class AudioCodec(val mimeType: String, val clockRate: Int) {
    OPUS("opus", 48000),
    PCMA("PCMA", 8000),
    PCMU("PCMU", 8000),
    G722("G722", 8000),
    ;

    companion object {
        /** Orden de prioridad por defecto: Opus primero (mejor calidad/ancho de banda). */
        val DEFAULT_PRIORITY = listOf(OPUS, PCMA, PCMU, G722)
    }
}

data class SipAccountConfig(
    val username: String,
    val password: String,
    val domain: String,
    val port: Int = 5060,
    val transport: SipTransport = SipTransport.UDP,
    val displayName: String = "",
    val srtpEnabled: Boolean = false,
    val stunEnabled: Boolean = false,
    val stunServer: String = "",
    val iceEnabled: Boolean = false,
    val turnEnabled: Boolean = false,
    val turnServer: String = "",
    val turnUsername: String = "",
    val turnPassword: String = "",
    val codecPriority: List<AudioCodec> = AudioCodec.DEFAULT_PRIORITY,
) {
    val isValid: Boolean
        get() = username.isNotBlank() && domain.isNotBlank() && port in 1..65535

    fun toPreferences(): SipAccountPreferences = SipAccountPreferences(
        username = username,
        domain = domain,
        port = port,
        transport = transport,
        displayName = displayName,
        srtpEnabled = srtpEnabled,
        stunEnabled = stunEnabled,
        stunServer = stunServer,
        iceEnabled = iceEnabled,
        turnEnabled = turnEnabled,
        turnServer = turnServer,
        turnUsername = turnUsername,
        codecPriority = codecPriority,
    )
}

/**
 * Subconjunto de [SipAccountConfig] sin contraseñas, apto para persistir en
 * DataStore. Las contraseñas (SIP y TURN) viven únicamente en
 * [cl.agnov.ameli.data.SecureCredentialStore].
 */
data class SipAccountPreferences(
    val username: String,
    val domain: String,
    val port: Int = 5060,
    val transport: SipTransport = SipTransport.UDP,
    val displayName: String = "",
    val srtpEnabled: Boolean = false,
    val stunEnabled: Boolean = false,
    val stunServer: String = "",
    val iceEnabled: Boolean = false,
    val turnEnabled: Boolean = false,
    val turnServer: String = "",
    val turnUsername: String = "",
    val codecPriority: List<AudioCodec> = AudioCodec.DEFAULT_PRIORITY,
) {
    fun toAccountConfig(password: String, turnPassword: String): SipAccountConfig = SipAccountConfig(
        username = username,
        password = password,
        domain = domain,
        port = port,
        transport = transport,
        displayName = displayName,
        srtpEnabled = srtpEnabled,
        stunEnabled = stunEnabled,
        stunServer = stunServer,
        iceEnabled = iceEnabled,
        turnEnabled = turnEnabled,
        turnServer = turnServer,
        turnUsername = turnUsername,
        turnPassword = turnPassword,
        codecPriority = codecPriority,
    )
}
