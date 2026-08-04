package cl.agnov.ameli.sip

import android.content.Context
import cl.agnov.ameli.sip.model.SipRegistrationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.CallStats
import org.linphone.core.Core
import org.linphone.core.Factory
import org.linphone.core.RegistrationState

/**
 * Propietario único de la instancia [Core] de Liblinphone.
 *
 * Todo el resto de la capa SIP ([SipAccountManager], [CallManager],
 * [AudioRouteManager]) opera sobre el [Core] expuesto aquí. No debe crearse
 * más de una instancia de [Core] en toda la aplicación.
 */
object LinphoneManager {

    private var coreInstance: Core? = null
    private var listener: LinphoneCoreListener? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var retryJob: Job? = null
    private var retryAttempt = 0

    private val _registrationState = MutableStateFlow(SipRegistrationState.NOT_REGISTERED)
    val registrationState: StateFlow<SipRegistrationState> = _registrationState.asStateFlow()

    private val _newVoicemailCount = MutableStateFlow(0)
    /** Cantidad de mensajes de voz nuevos, según el último aviso MWI recibido de la cuenta. */
    val newVoicemailCount: StateFlow<Int> = _newVoicemailCount.asStateFlow()

    /** Callback interno usado por [CallManager] para recibir cambios de estado de llamada. */
    var onCallStateChanged: ((Call, Call.State, String?) -> Unit)? = null

    /** Callback interno usado por [CallManager] para recibir estadísticas de la llamada en curso. */
    var onCallStatsUpdated: ((Call, CallStats) -> Unit)? = null

    val isStarted: Boolean
        get() = coreInstance != null

    val core: Core
        get() = requireNotNull(coreInstance) { "LinphoneManager.start() no ha sido llamado." }

    /** Llamado por [SipAccountManager] cuando la cuenta reporta un cambio de MWI. */
    fun updateNewVoicemailCount(count: Int) {
        _newVoicemailCount.value = count
    }

    fun start(context: Context) {
        if (coreInstance != null) return

        val appContext = context.applicationContext
        val newListener = LinphoneCoreListener(
            onAccountRegistrationStateChanged = { account, state, message ->
                onRegistrationStateChanged(account, state, message)
            },
            onCallStateChanged = { call, state, message ->
                onCallStateChanged?.invoke(call, state, message)
            },
            onNetworkReachableChanged = { reachable ->
                if (reachable) {
                    coreInstance?.refreshRegisters()
                }
            },
            onCallStatsUpdated = { call, stats ->
                onCallStatsUpdated?.invoke(call, stats)
            },
        )

        val newCore = Factory.instance().createCore(null, null, appContext)
        newCore.isAutoIterateEnabled = true
        newCore.addListener(newListener)
        newCore.start()

        listener = newListener
        coreInstance = newCore
    }

    fun stop() {
        val runningCore = coreInstance ?: return
        cancelRetry()
        listener?.let { runningCore.removeListener(it) }
        runningCore.stop()
        listener = null
        coreInstance = null
        _registrationState.value = SipRegistrationState.NOT_REGISTERED
    }

    private fun onRegistrationStateChanged(account: Account, state: RegistrationState, message: String?) {
        val newState = SipRegistrationState.from(state, account.error, message)
        _registrationState.value = newState

        when (newState) {
            // Fallas de red/servidor: reintentar con backoff exponencial.
            SipRegistrationState.SERVER_UNAVAILABLE, SipRegistrationState.UNKNOWN_ERROR -> scheduleRetry()
            // Registrado, registrando, desconectado deliberadamente, error de
            // credenciales o de certificado: no tiene sentido reintentar solo,
            // así que se cancela cualquier backoff pendiente y se reinicia el
            // contador para el próximo fallo real.
            else -> cancelRetry()
        }
    }

    private fun scheduleRetry() {
        if (retryJob?.isActive == true) return

        val delayMs = backoffDelayMillis(retryAttempt)
        retryAttempt++
        retryJob = managerScope.launch {
            delay(delayMs)
            coreInstance?.refreshRegisters()
        }
    }

    private fun cancelRetry() {
        retryJob?.cancel()
        retryJob = null
        retryAttempt = 0
    }

    private fun backoffDelayMillis(attempt: Int): Long {
        val exponentialMillis = BASE_RETRY_DELAY_MS * (1L shl attempt.coerceAtMost(MAX_BACKOFF_SHIFT))
        return exponentialMillis.coerceAtMost(MAX_RETRY_DELAY_MS)
    }

    private const val BASE_RETRY_DELAY_MS = 2_000L
    private const val MAX_RETRY_DELAY_MS = 60_000L
    private const val MAX_BACKOFF_SHIFT = 5
}
