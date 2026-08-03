package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.CallUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay

private fun secondTicker() = flow {
    while (true) {
        emit(Unit)
        delay(1000)
    }
}

/**
 * Expone el estado de la llamada activa con la duración actualizándose cada
 * segundo, y las acciones disponibles mientras la llamada está en curso.
 */
class ActiveCallViewModel(
    private val callController: CallController,
) : ViewModel() {

    val uiState: StateFlow<CallUiState?> = combine(
        callController.callState,
        secondTicker(),
    ) { call, _ -> call?.copy(durationSeconds = callController.currentDurationSeconds()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), callController.callState.value)

    fun hangup() {
        callController.hangup()
    }

    fun toggleMute() {
        callController.toggleMute()
    }

    fun toggleSpeaker() {
        callController.toggleSpeaker()
    }

    fun sendDtmf(digit: Char) {
        callController.sendDtmf(digit)
    }
}
