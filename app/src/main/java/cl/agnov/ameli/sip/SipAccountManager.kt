package cl.agnov.ameli.sip

import cl.agnov.ameli.sip.model.AudioCodec
import cl.agnov.ameli.sip.model.SipAccountConfig
import cl.agnov.ameli.sip.model.SipTransport
import org.linphone.core.Account
import org.linphone.core.AccountListenerStub
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.MessageWaitingIndication
import org.linphone.core.PayloadType
import org.linphone.core.TransportType

/** Permite sustituir [SipAccountManager] por un fake en pruebas unitarias. */
interface AccountConfigurator {
    fun applyAccount(config: SipAccountConfig): Result<Unit>
}

/**
 * Traduce un [SipAccountConfig] a las llamadas reales de Liblinphone
 * (AccountParams/Account/AuthInfo/NatPolicy) y lo registra en el
 * [LinphoneManager.core].
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

            configureNatPolicy(core, factory, config)?.let { accountParams.natPolicy = it }

            core.setMediaEncryption(
                if (config.srtpEnabled) MediaEncryption.SRTP else MediaEncryption.None,
            )

            applyCodecPriority(core, config.codecPriority)

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
            account.addListener(object : AccountListenerStub() {
                override fun onMessageWaitingIndicationChanged(account: Account, mwi: MessageWaitingIndication) {
                    LinphoneManager.updateNewVoicemailCount(mwi.nbNew)
                }
            })
            core.addAccount(account)
            core.defaultAccount = account

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Configura STUN/ICE/TURN. El SDK modela STUN y TURN como un único
     * servidor ([org.linphone.core.NatPolicy.getStunServer]) distinguido por
     * los flags [org.linphone.core.NatPolicy.isStunEnabled]/[org.linphone.core.NatPolicy.isTurnEnabled],
     * así que si TURN está activo se usa su servidor (un servidor TURN
     * también responde STUN); si no, se usa el servidor STUN configurado.
     */
    private fun configureNatPolicy(core: Core, factory: Factory, config: SipAccountConfig): org.linphone.core.NatPolicy? {
        if (!config.stunEnabled && !config.iceEnabled && !config.turnEnabled) return null

        val natPolicy = core.createNatPolicy()
        natPolicy.isIceEnabled = config.iceEnabled

        if (config.turnEnabled && config.turnServer.isNotBlank()) {
            natPolicy.stunServer = config.turnServer
            natPolicy.stunServerUsername = config.turnUsername
            natPolicy.isTurnEnabled = true
            natPolicy.isUdpTurnTransportEnabled = true

            if (config.turnUsername.isNotBlank()) {
                val turnAuthInfo = factory.createAuthInfo(
                    config.turnUsername,
                    null,
                    config.turnPassword,
                    null,
                    null,
                    hostOf(config.turnServer),
                )
                core.addAuthInfo(turnAuthInfo)
            }
        } else if (config.stunEnabled && config.stunServer.isNotBlank()) {
            natPolicy.stunServer = config.stunServer
            natPolicy.isStunEnabled = true
        }

        return natPolicy
    }

    private fun hostOf(serverAddress: String): String = serverAddress.substringBefore(':')

    /**
     * Reordena [Core.getAudioPayloadTypes] según la prioridad elegida por el
     * usuario, deshabilitando los códecs que no estén en la lista.
     */
    private fun applyCodecPriority(core: Core, priority: List<AudioCodec>) {
        val available = core.audioPayloadTypes.toMutableList()
        val ordered = mutableListOf<PayloadType>()

        for (codec in priority) {
            val match = available.firstOrNull {
                it.mimeType.equals(codec.mimeType, ignoreCase = true) && it.clockRate == codec.clockRate
            } ?: continue
            match.enable(true)
            ordered.add(match)
            available.remove(match)
        }

        // Los códecs no priorizados por el usuario quedan deshabilitados pero
        // se conservan en la lista (Liblinphone los ignora si están off).
        available.forEach { it.enable(false) }
        ordered.addAll(available)

        core.setAudioPayloadTypes(ordered.toTypedArray())
    }

    private fun SipTransport.toLinphoneTransportType(): TransportType = when (this) {
        SipTransport.UDP -> TransportType.Udp
        SipTransport.TCP -> TransportType.Tcp
        SipTransport.TLS -> TransportType.Tls
    }
}
