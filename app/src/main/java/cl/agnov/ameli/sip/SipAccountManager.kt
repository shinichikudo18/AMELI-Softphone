package cl.agnov.ameli.sip

import cl.agnov.ameli.sip.model.SipAccountConfig
import cl.agnov.ameli.sip.model.SipTransport
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.TransportType

/** Permite sustituir [SipAccountManager] por un fake en pruebas unitarias. */
interface AccountConfigurator {
    fun applyAccount(config: SipAccountConfig): Result<Unit>
}

/**
 * Traduce un [SipAccountConfig] a las llamadas reales de Liblinphone
 * (AccountParams/Account/AuthInfo) y lo registra en el [LinphoneManager.core].
 */
class SipAccountManager : AccountConfigurator {

    override fun applyAccount(config: SipAccountConfig): Result<Unit> {
        if (!config.isValid) {
            return Result.failure(IllegalArgumentException("Configuración de cuenta SIP incompleta"))
        }

        return try {
            val core = LinphoneManager.core
            val factory = Factory.instance()

            val identityAddress = factory.createAddress("sip:${config.username}@${config.domain}")
                ?: return Result.failure(IllegalArgumentException("No se pudo construir la dirección SIP del usuario"))
            if (config.displayName.isNotBlank()) {
                identityAddress.setDisplayName(config.displayName)
            }

            val serverAddress = factory.createAddress("sip:${config.domain}:${config.port}")
                ?: return Result.failure(IllegalArgumentException("No se pudo construir la dirección del servidor SIP"))
            serverAddress.setTransport(config.transport.toLinphoneTransportType())

            val accountParams = core.createAccountParams()
            accountParams.setIdentityAddress(identityAddress)
            accountParams.setServerAddress(serverAddress)
            accountParams.isRegisterEnabled = true

            if (config.stunEnabled && config.stunServer.isNotBlank()) {
                val natPolicy = core.createNatPolicy()
                natPolicy.stunServer = config.stunServer
                natPolicy.isStunEnabled = true
                natPolicy.isIceEnabled = true
                accountParams.natPolicy = natPolicy
            }

            core.setMediaEncryption(
                if (config.srtpEnabled) MediaEncryption.SRTP else MediaEncryption.None,
            )

            // realm = null acepta el realm que reporte el servidor en el desafío de autenticación.
            val authInfo = factory.createAuthInfo(
                config.username,
                null,
                config.password,
                null,
                null,
                config.domain,
            )
            core.addAuthInfo(authInfo)

            val account = core.createAccount(accountParams)
            core.addAccount(account)
            core.defaultAccount = account

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun SipTransport.toLinphoneTransportType(): TransportType = when (this) {
        SipTransport.UDP -> TransportType.Udp
        SipTransport.TCP -> TransportType.Tcp
        SipTransport.TLS -> TransportType.Tls
    }
}
