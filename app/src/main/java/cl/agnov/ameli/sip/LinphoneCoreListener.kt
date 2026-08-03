package cl.agnov.ameli.sip

import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.RegistrationState

/**
 * Traduce los callbacks nativos de [Core] a las funciones que le interesan al
 * resto de la capa SIP, manteniendo aislada la dependencia directa del SDK.
 */
class LinphoneCoreListener(
    private val onAccountRegistrationStateChanged: (Account, RegistrationState, String?) -> Unit,
    private val onCallStateChanged: (Call, Call.State, String?) -> Unit,
    private val onNetworkReachableChanged: (Boolean) -> Unit,
) : CoreListenerStub() {

    override fun onAccountRegistrationStateChanged(
        core: Core,
        account: Account,
        state: RegistrationState,
        message: String,
    ) {
        onAccountRegistrationStateChanged.invoke(account, state, message)
    }

    override fun onCallStateChanged(core: Core, call: Call, state: Call.State, message: String) {
        onCallStateChanged.invoke(call, state, message)
    }

    override fun onNetworkReachable(core: Core, reachable: Boolean) {
        onNetworkReachableChanged.invoke(reachable)
    }
}
