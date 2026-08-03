package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.SipRegistrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado y acciones del teclado numérico para iniciar llamadas salientes.
 */
class DialerViewModel(
    private val callController: CallController,
    registrationState: StateFlow<SipRegistrationState>,
) : ViewModel() {

    val registrationState: StateFlow<SipRegistrationState> = registrationState

    private val _dialedAddress = MutableStateFlow("")
    val dialedAddress: StateFlow<String> = _dialedAddress.asStateFlow()

    private val _callError = MutableStateFlow<String?>(null)
    val callError: StateFlow<String?> = _callError.asStateFlow()

    fun onDialedAddressChanged(value: String) {
        _dialedAddress.value = value
        _callError.value = null
    }

    fun onKeyPressed(key: Char) {
        onDialedAddressChanged(_dialedAddress.value + key)
    }

    fun onBackspace() {
        onDialedAddressChanged(_dialedAddress.value.dropLast(1))
    }

    fun call() {
        val address = _dialedAddress.value.trim()
        if (address.isEmpty()) {
            _callError.value = "Ingresa un número o dirección SIP"
            return
        }
        val result = callController.call(address)
        _callError.value = result.exceptionOrNull()?.message
    }
}
