package cl.agnov.ameli.sip

import cl.agnov.ameli.data.CallHistoryRepository
import cl.agnov.ameli.data.DoNotDisturbState
import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.sip.model.CallDirection
import cl.agnov.ameli.sip.model.CallHistoryRecord
import cl.agnov.ameli.sip.model.CallQualityStats
import cl.agnov.ameli.sip.model.CallResult
import cl.agnov.ameli.sip.model.CallUiState
import cl.agnov.ameli.sip.model.IceConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.linphone.core.Call
import org.linphone.core.CallStats
import org.linphone.core.Reason
import org.linphone.core.StreamType

/** Permite sustituir [CallManager] por un fake en pruebas unitarias. */
interface CallController {
    /** Llamada en primer plano: la que controlan answer/decline/hangup/mute/speaker/dtmf. */
    val callState: StateFlow<CallUiState?>

    /** La otra llamada simultánea, si existe (en espera, timbrando o siendo transferida). */
    val secondaryCallState: StateFlow<CallUiState?>
    val callQualityStats: StateFlow<CallQualityStats?>

    fun call(addressOrNumber: String): Result<Unit>
    fun answer(): Result<Unit>
    fun decline(): Result<Unit>
    fun hangup()
    fun silenceRinger()
    fun toggleMute()
    fun toggleSpeaker()
    fun sendDtmf(digit: Char): Result<Unit>
    fun currentDurationSeconds(): Int

    /** Pone en espera la llamada en curso e inicia una segunda hacia [addressOrNumber]. */
    fun startSecondCall(addressOrNumber: String): Result<Unit>

    /** Intercambia cuál de las dos llamadas está en primer plano (pausa una, reanuda la otra). */
    fun swapCalls(): Result<Unit>

    /** Contesta la llamada secundaria (p.ej. una llamada en espera) y la pasa a primer plano. */
    fun answerSecondary(): Result<Unit>
    fun declineSecondary(): Result<Unit>
    fun hangupSecondary()

    /** Transferencia ciega de la llamada en primer plano a [addressOrNumber]. */
    fun transferForegroundTo(addressOrNumber: String): Result<Unit>

    /** Transferencia consultiva: une la llamada en espera con la de primer plano. */
    fun completeConsultativeTransfer(): Result<Unit>
}

/**
 * Gestiona el ciclo de vida de hasta dos llamadas simultáneas (primer plano
 * + en espera) a partir de los eventos que [LinphoneManager] reenvía desde
 * [org.linphone.core.CoreListenerStub].
 */
class CallManager(
    private val audioRouteController: AudioRouteController,
    private val historyRepository: CallHistoryRepository,
    private val doNotDisturbState: DoNotDisturbState,
) : CallController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _callState = MutableStateFlow<CallUiState?>(null)
    override val callState: StateFlow<CallUiState?> = _callState.asStateFlow()

    private val _secondaryCallState = MutableStateFlow<CallUiState?>(null)
    override val secondaryCallState: StateFlow<CallUiState?> = _secondaryCallState.asStateFlow()

    private val _callQualityStats = MutableStateFlow<CallQualityStats?>(null)
    override val callQualityStats: StateFlow<CallQualityStats?> = _callQualityStats.asStateFlow()

    private val _doNotDisturbEnabled = MutableStateFlow(false)

    /** Todas las llamadas activas conocidas, por Call-ID SIP (estable a través de la vida del Call). */
    private val calls = LinkedHashMap<String, Call>()
    private var foregroundCallId: String? = null

    init {
        LinphoneManager.onCallStateChanged = { call, state, _ -> onCallStateChanged(call, state) }
        LinphoneManager.onCallStatsUpdated = { call, stats -> onCallStatsUpdated(call, stats) }
        scope.launch { doNotDisturbState.isEnabled.collect { _doNotDisturbEnabled.value = it } }
    }

    override fun call(addressOrNumber: String): Result<Unit> {
        val core = LinphoneManager.core
        val account = core.defaultAccount
            ?: return Result.failure(IllegalStateException("No hay una cuenta SIP configurada"))
        val address = account.normalizeSipUri(addressOrNumber)
            ?: return Result.failure(IllegalArgumentException("Número o dirección SIP inválida"))

        val call = core.inviteAddress(address)
            ?: return Result.failure(IllegalStateException("No se pudo iniciar la llamada"))
        trackCall(call, asForeground = true)
        return Result.success(Unit)
    }

    override fun answer(): Result<Unit> {
        val call = foregroundCall() ?: return Result.failure(IllegalStateException("No hay una llamada entrante"))
        call.accept()
        return Result.success(Unit)
    }

    override fun decline(): Result<Unit> {
        val call = foregroundCall() ?: return Result.failure(IllegalStateException("No hay una llamada entrante"))
        call.decline(Reason.Declined)
        return Result.success(Unit)
    }

    override fun hangup() {
        foregroundCall()?.terminate()
    }

    override fun silenceRinger() {
        LinphoneManager.core.stopRinging()
    }

    override fun toggleMute() {
        audioRouteController.setMicMuted(!audioRouteController.isMicMuted())
        refreshStates()
    }

    override fun toggleSpeaker() {
        audioRouteController.setSpeakerEnabled(!audioRouteController.isSpeakerOn())
        refreshStates()
    }

    override fun sendDtmf(digit: Char): Result<Unit> {
        val call = foregroundCall() ?: return Result.failure(IllegalStateException("No hay una llamada activa"))
        call.sendDtmf(digit)
        return Result.success(Unit)
    }

    override fun currentDurationSeconds(): Int = foregroundCall()?.duration ?: 0

    override fun startSecondCall(addressOrNumber: String): Result<Unit> {
        if (calls.size >= MAX_SIMULTANEOUS_CALLS) {
            return Result.failure(IllegalStateException("Ya hay dos llamadas activas"))
        }
        foregroundCall()?.pause()
        return call(addressOrNumber)
    }

    override fun swapCalls(): Result<Unit> {
        val secondaryId = otherCallId() ?: return Result.failure(IllegalStateException("No hay otra llamada"))
        foregroundCall()?.pause()
        calls[secondaryId]?.resume()
        foregroundCallId = secondaryId
        refreshStates()
        return Result.success(Unit)
    }

    override fun answerSecondary(): Result<Unit> {
        val secondaryId = otherCallId() ?: return Result.failure(IllegalStateException("No hay una llamada en espera"))
        val call = calls[secondaryId] ?: return Result.failure(IllegalStateException("No hay una llamada en espera"))
        call.accept()
        foregroundCallId = secondaryId
        refreshStates()
        return Result.success(Unit)
    }

    override fun declineSecondary(): Result<Unit> {
        val secondaryId = otherCallId() ?: return Result.failure(IllegalStateException("No hay una llamada en espera"))
        val call = calls[secondaryId] ?: return Result.failure(IllegalStateException("No hay una llamada en espera"))
        call.decline(Reason.Declined)
        return Result.success(Unit)
    }

    override fun hangupSecondary() {
        otherCallId()?.let { calls[it]?.terminate() }
    }

    override fun transferForegroundTo(addressOrNumber: String): Result<Unit> {
        val call = foregroundCall() ?: return Result.failure(IllegalStateException("No hay una llamada activa"))
        val account = LinphoneManager.core.defaultAccount
            ?: return Result.failure(IllegalStateException("No hay una cuenta SIP configurada"))
        val address = account.normalizeSipUri(addressOrNumber)
            ?: return Result.failure(IllegalArgumentException("Número o dirección SIP inválida"))
        call.transferTo(address)
        return Result.success(Unit)
    }

    override fun completeConsultativeTransfer(): Result<Unit> {
        val heldId = otherCallId() ?: return Result.failure(IllegalStateException("Se necesitan dos llamadas para transferir"))
        val heldCall = calls[heldId] ?: return Result.failure(IllegalStateException("Se necesitan dos llamadas para transferir"))
        val targetCall = foregroundCall() ?: return Result.failure(IllegalStateException("Se necesitan dos llamadas para transferir"))
        heldCall.transferToAnother(targetCall)
        return Result.success(Unit)
    }

    /** El SIP Call-ID es estable durante toda la vida del Call; si el SDK no lo expone, se usa un id de respaldo. */
    private fun Call.stableId(): String = callLog.callId ?: System.identityHashCode(this).toString()

    private fun foregroundCall(): Call? = foregroundCallId?.let { calls[it] }

    private fun otherCallId(): String? = calls.keys.firstOrNull { it != foregroundCallId }

    private fun trackCall(call: Call, asForeground: Boolean) {
        val callId = call.stableId()
        calls[callId] = call
        if (asForeground || foregroundCallId == null) {
            foregroundCallId = callId
        }
        refreshStates()
    }

    private fun onCallStateChanged(call: Call, state: Call.State) {
        if (state == Call.State.IncomingReceived && _doNotDisturbEnabled.value) {
            // No Molestar: se rechaza antes de timbrar/notificar. La llamada
            // igual queda registrada en el historial cuando llegue Released,
            // ya que recordHistory() usa el `call` recibido, no el mapa `calls`.
            call.decline(Reason.Busy)
            return
        }

        val callId = call.stableId()

        if (state == Call.State.End || state == Call.State.Released || state == Call.State.Error) {
            if (state == Call.State.Released) {
                recordHistory(call)
            }
            calls.remove(callId)
            if (foregroundCallId == callId) {
                foregroundCallId = calls.keys.firstOrNull()
                // Si queda una sola llamada y estaba en espera, se reanuda
                // automáticamente al terminar la otra (comportamiento
                // estándar de telefonía).
                foregroundCall()?.let { remaining ->
                    if (remaining.state == Call.State.Paused) remaining.resume()
                }
            }
            if (callId == foregroundCallId || calls.isEmpty()) {
                _callQualityStats.value = null
            }
            refreshStates()
            return
        }

        calls[callId] = call
        if (foregroundCallId == null) {
            foregroundCallId = callId
        }
        refreshStates()
    }

    private fun onCallStatsUpdated(call: Call, stats: CallStats) {
        if (call.stableId() != foregroundCallId || stats.type != StreamType.Audio) return

        _callQualityStats.value = CallQualityStats(
            codecName = call.currentParams.usedAudioPayloadType?.mimeType,
            packetLossPercent = stats.receiverLossRate,
            jitterSeconds = stats.receiverInterarrivalJitter,
            roundTripSeconds = stats.roundTripDelay,
            iceState = IceConnectionState.from(stats.iceState),
        )
    }

    private fun recordHistory(call: Call) {
        val log = call.callLog
        val remoteAddress = log.remoteAddress
        val record = CallHistoryRecord(
            remoteAddress = remoteAddress.asStringUriOnly(),
            remoteDisplayName = remoteAddress.displayName?.takeIf { it.isNotBlank() },
            direction = if (log.dir == Call.Dir.Outgoing) CallDirection.OUTGOING else CallDirection.INCOMING,
            startDateEpochSeconds = log.startDate,
            durationSeconds = log.duration,
            result = CallResult.from(log.status),
        )
        scope.launch { historyRepository.record(record) }
    }

    private fun refreshStates() {
        _callState.value = foregroundCall()?.let { toUiState(it) }
        _secondaryCallState.value = otherCallId()?.let { calls[it] }?.let { toUiState(it) }
    }

    private fun toUiState(call: Call): CallUiState {
        val remoteAddress = call.remoteAddress
        return CallUiState(
            callId = call.stableId(),
            direction = if (call.dir == Call.Dir.Outgoing) CallDirection.OUTGOING else CallDirection.INCOMING,
            remoteAddress = remoteAddress.asStringUriOnly(),
            remoteDisplayName = remoteAddress.displayName?.takeIf { it.isNotBlank() },
            connectionState = CallConnectionState.from(call.state),
            durationSeconds = call.duration,
            isMicMuted = audioRouteController.isMicMuted(),
            isSpeakerOn = audioRouteController.isSpeakerOn(),
        )
    }

    private companion object {
        const val MAX_SIMULTANEOUS_CALLS = 2
    }
}
