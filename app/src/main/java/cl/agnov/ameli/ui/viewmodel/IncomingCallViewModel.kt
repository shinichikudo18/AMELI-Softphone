package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.CallUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Expone la llamada entrante actual y las acciones para contestarla,
 * rechazarla o silenciar el timbre sin colgar.
 */
class IncomingCallViewModel(
    private val callController: CallController,
) : ViewModel() {

    val callState: StateFlow<CallUiState?> = callController.callState

    private val _isRingerSilenced = MutableStateFlow(false)
    val isRingerSilenced: StateFlow<Boolean> = _isRingerSilenced.asStateFlow()

    init {
        // Si llega una nueva llamada entrante, el timbre vuelve a sonar.
        viewModelScope.launch {
            callController.callState.collect { _isRingerSilenced.value = false }
        }
    }

    fun answer() {
        callController.answer()
    }

    fun decline() {
        callController.decline()
    }

    fun silenceRinger() {
        callController.silenceRinger()
        _isRingerSilenced.value = true
    }
}
