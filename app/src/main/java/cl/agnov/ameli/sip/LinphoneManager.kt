package cl.agnov.ameli.sip

import android.content.Context
import cl.agnov.ameli.sip.model.SipRegistrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Account
import org.linphone.core.Call
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

    private val _registrationState = MutableStateFlow(SipRegistrationState.NOT_REGISTERED)
    val registrationState: StateFlow<SipRegistrationState> = _registrationState.asStateFlow()

    /** Callback interno usado por [CallManager] para recibir cambios de estado de llamada. */
    var onCallStateChanged: ((Call, Call.State, String?) -> Unit)? = null

    val isStarted: Boolean
        get() = coreInstance != null

    val core: Core
        get() = requireNotNull(coreInstance) { "LinphoneManager.start() no ha sido llamado." }

    fun start(context: Context) {
        if (coreInstance != null) return

        val appContext = context.applicationContext
        val newListener = LinphoneCoreListener(
            onAccountRegistrationStateChanged = { account, state, message ->
                _registrationState.value = SipRegistrationState.from(state, account.error, message)
            },
            onCallStateChanged = { call, state, message ->
                onCallStateChanged?.invoke(call, state, message)
            },
            onNetworkReachableChanged = { reachable ->
                if (reachable) {
                    coreInstance?.refreshRegisters()
                }
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
        listener?.let { runningCore.removeListener(it) }
        runningCore.stop()
        listener = null
        coreInstance = null
        _registrationState.value = SipRegistrationState.NOT_REGISTERED
    }
}
