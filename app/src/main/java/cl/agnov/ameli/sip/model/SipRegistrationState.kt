package cl.agnov.ameli.sip.model

import org.linphone.core.Reason
import org.linphone.core.RegistrationState

/**
 * Estado de registro SIP traducido a mensajes comprensibles para la UI,
 * a partir del [RegistrationState] y [Reason] reales expuestos por Liblinphone.
 */
enum class SipRegistrationState {
    NOT_REGISTERED,
    REGISTERING,
    REGISTERED,
    DISCONNECTED,
    AUTHENTICATION_ERROR,
    SERVER_UNAVAILABLE,
    CERTIFICATE_ERROR,
    UNKNOWN_ERROR,
    ;

    companion object {
        fun from(state: RegistrationState, reason: Reason?, message: String?): SipRegistrationState {
            return when (state) {
                RegistrationState.None -> NOT_REGISTERED
                RegistrationState.Progress, RegistrationState.Refreshing -> REGISTERING
                RegistrationState.Ok -> REGISTERED
                RegistrationState.Cleared -> DISCONNECTED
                RegistrationState.Failed -> mapFailure(reason, message)
            }
        }

        private fun mapFailure(reason: Reason?, message: String?): SipRegistrationState {
            val lowerMessage = message?.lowercase().orEmpty()
            if (lowerMessage.contains("certificate") || lowerMessage.contains("tls")) {
                return CERTIFICATE_ERROR
            }
            return when (reason) {
                Reason.Unauthorized, Reason.Forbidden -> AUTHENTICATION_ERROR
                Reason.IOError, Reason.ServerTimeout, Reason.TemporarilyUnavailable -> SERVER_UNAVAILABLE
                else -> UNKNOWN_ERROR
            }
        }
    }
}
