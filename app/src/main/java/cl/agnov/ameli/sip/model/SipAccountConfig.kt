package cl.agnov.ameli.sip.model

enum class SipTransport { UDP, TCP, TLS }

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
    )
}

/**
 * Subconjunto de [SipAccountConfig] sin la contraseña, apto para persistir
 * en DataStore. La contraseña vive únicamente en [cl.agnov.ameli.data.SecureCredentialStore].
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
) {
    fun toAccountConfig(password: String): SipAccountConfig = SipAccountConfig(
        username = username,
        password = password,
        domain = domain,
        port = port,
        transport = transport,
        displayName = displayName,
        srtpEnabled = srtpEnabled,
        stunEnabled = stunEnabled,
        stunServer = stunServer,
    )
}
