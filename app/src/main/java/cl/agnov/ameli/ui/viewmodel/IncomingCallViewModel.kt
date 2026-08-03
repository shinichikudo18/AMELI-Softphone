package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.CallUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Expone la llamada entrante actual y las acciones para contestarla o
 * rechazarla.
 */
class IncomingCallViewModel(
    private val callController: CallController,
) : ViewModel() {

    val callState: StateFlow<CallUiState?> = callController.callState

    fun answer() {
        callController.answer()
    }

    fun decline() {
        callController.decline()
    }
}
