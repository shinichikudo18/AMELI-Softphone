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
    val callState: StateFlow<CallUiState?>
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
}

/**
 * Gestiona el ciclo de vida de la llamada activa a partir de los eventos que
 * [LinphoneManager] reenvía desde [org.linphone.core.CoreListenerStub].
 */
class CallManager(
    private val audioRouteController: AudioRouteController,
    private val historyRepository: CallHistoryRepository,
    private val doNotDisturbState: DoNotDisturbState,
) : CallController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _callState = MutableStateFlow<CallUiState?>(null)
    override val callState: StateFlow<CallUiState?> = _callState.asStateFlow()

    private val _callQualityStats = MutableStateFlow<CallQualityStats?>(null)
    override val callQualityStats: StateFlow<CallQualityStats?> = _callQualityStats.asStateFlow()

    private val _doNotDisturbEnabled = MutableStateFlow(false)

    private var activeCall: Call? = null
    private var lastKnownState: Call.State? = null

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
        activeCall = call
        return Result.success(Unit)
    }

    override fun answer(): Result<Unit> {
        val call = activeCall ?: return Result.failure(IllegalStateException("No hay una llamada entrante"))
        call.accept()
        return Result.success(Unit)
    }

    override fun decline(): Result<Unit> {
        val call = activeCall ?: return Result.failure(IllegalStateException("No hay una llamada entrante"))
        call.decline(Reason.Declined)
        return Result.success(Unit)
    }

    override fun hangup() {
        activeCall?.terminate()
    }

    override fun silenceRinger() {
        LinphoneManager.core.stopRinging()
    }

    override fun toggleMute() {
        audioRouteController.setMicMuted(!audioRouteController.isMicMuted())
        refreshCallState()
    }

    override fun toggleSpeaker() {
        audioRouteController.setSpeakerEnabled(!audioRouteController.isSpeakerOn())
        refreshCallState()
    }

    override fun sendDtmf(digit: Char): Result<Unit> {
        val call = activeCall ?: return Result.failure(IllegalStateException("No hay una llamada activa"))
        call.sendDtmf(digit)
        return Result.success(Unit)
    }

    override fun currentDurationSeconds(): Int = activeCall?.duration ?: 0

    private fun onCallStateChanged(call: Call, state: Call.State) {
        if (state == Call.State.IncomingReceived && _doNotDisturbEnabled.value) {
            // No Molestar: se rechaza antes de timbrar/notificar. La llamada
            // igual queda registrada en el historial cuando llegue Released,
            // ya que recordHistory() usa el `call` recibido, no `activeCall`.
            call.decline(Reason.Busy)
            return
        }

        activeCall = call
        lastKnownState = state
        _callState.value = toUiState(call, state)

        if (state == Call.State.Released) {
            recordHistory(call)
        }

        if (state == Call.State.End || state == Call.State.Released || state == Call.State.Error) {
            activeCall = null
            lastKnownState = null
            _callQualityStats.value = null
        }
    }

    private fun onCallStatsUpdated(call: Call, stats: CallStats) {
        if (call != activeCall || stats.type != StreamType.Audio) return

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

    private fun refreshCallState() {
        val call = activeCall ?: return
        val state = lastKnownState ?: return
        _callState.value = toUiState(call, state)
    }

    private fun toUiState(call: Call, state: Call.State): CallUiState {
        val remoteAddress = call.remoteAddress
        return CallUiState(
            direction = if (call.dir == Call.Dir.Outgoing) CallDirection.OUTGOING else CallDirection.INCOMING,
            remoteAddress = remoteAddress.asStringUriOnly(),
            remoteDisplayName = remoteAddress.displayName?.takeIf { it.isNotBlank() },
            connectionState = CallConnectionState.from(state),
            durationSeconds = call.duration,
            isMicMuted = audioRouteController.isMicMuted(),
            isSpeakerOn = audioRouteController.isSpeakerOn(),
        )
    }
}
