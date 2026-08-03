package cl.agnov.ameli.sip

import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.sip.model.CallDirection
import cl.agnov.ameli.sip.model.CallUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Call

/** Permite sustituir [CallManager] por un fake en pruebas unitarias. */
interface CallController {
    val callState: StateFlow<CallUiState?>
    fun call(addressOrNumber: String): Result<Unit>
    fun hangup()
    fun currentDurationSeconds(): Int
}

/**
 * Gestiona el ciclo de vida de la llamada activa a partir de los eventos que
 * [LinphoneManager] reenvía desde [org.linphone.core.CoreListenerStub].
 */
class CallManager : CallController {

    private val _callState = MutableStateFlow<CallUiState?>(null)
    override val callState: StateFlow<CallUiState?> = _callState.asStateFlow()

    private var activeCall: Call? = null

    init {
        LinphoneManager.onCallStateChanged = { call, state, _ -> onCallStateChanged(call, state) }
    }

    override fun call(addressOrNumber: String): Result<Unit> {
        val core = LinphoneManager.core
        val account = core.defaultAccount
            ?: return Result.failure(IllegalStateException("No hay una cuenta SIP configurada"))
        val address = account.normalizeSipUri(addressOrNumber)
            ?: return Result.failure(IllegalArgumentException("Número o dirección SIP inválida"))

        val call = core.inviteAddress(address)
            ?: return Result.failure(IllegalStateException("No se pudo iniciar la llamada"))
        activeCall = call
        return Result.success(Unit)
    }

    override fun hangup() {
        activeCall?.terminate()
    }

    override fun currentDurationSeconds(): Int = activeCall?.duration ?: 0

    private fun onCallStateChanged(call: Call, state: Call.State) {
        activeCall = call
        _callState.value = toUiState(call, state)

        if (state == Call.State.End || state == Call.State.Released || state == Call.State.Error) {
            activeCall = null
        }
    }

    private fun toUiState(call: Call, state: Call.State): CallUiState {
        val remoteAddress = call.remoteAddress
        return CallUiState(
            direction = if (call.dir == Call.Dir.Outgoing) CallDirection.OUTGOING else CallDirection.INCOMING,
            remoteAddress = remoteAddress.asStringUriOnly(),
            remoteDisplayName = remoteAddress.displayName?.takeIf { it.isNotBlank() },
            connectionState = CallConnectionState.from(state),
            durationSeconds = call.duration,
        )
    }
}
