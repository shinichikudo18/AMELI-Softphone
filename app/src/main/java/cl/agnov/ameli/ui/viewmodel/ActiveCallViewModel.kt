package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.CallQualityStats
import cl.agnov.ameli.sip.model.CallUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

private fun secondTicker() = flow {
    while (true) {
        emit(Unit)
        delay(1000)
    }
}

/**
 * Expone el estado de la llamada activa (y, si existe, una segunda llamada
 * en espera) con la duración actualizándose cada segundo, y las acciones
 * disponibles: colgar, mute, altavoz, DTMF, segunda llamada, transferencia.
 */
class ActiveCallViewModel(
    private val callController: CallController,
) : ViewModel() {

    val uiState: StateFlow<CallUiState?> = combine(
        callController.callState,
        secondTicker(),
    ) { call, _ -> call?.copy(durationSeconds = callController.currentDurationSeconds()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), callController.callState.value)

    val secondaryCallState: StateFlow<CallUiState?> = callController.secondaryCallState

    val qualityStats: StateFlow<CallQualityStats?> = callController.callQualityStats

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

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

    fun startSecondCall(addressOrNumber: String) {
        reportIfFailed(callController.startSecondCall(addressOrNumber))
    }

    fun swapCalls() {
        reportIfFailed(callController.swapCalls())
    }

    fun answerSecondary() {
        reportIfFailed(callController.answerSecondary())
    }

    fun declineSecondary() {
        reportIfFailed(callController.declineSecondary())
    }

    fun hangupSecondary() {
        callController.hangupSecondary()
    }

    fun transferForegroundTo(addressOrNumber: String) {
        reportIfFailed(callController.transferForegroundTo(addressOrNumber))
    }

    fun completeConsultativeTransfer() {
        reportIfFailed(callController.completeConsultativeTransfer())
    }

    private fun reportIfFailed(result: Result<Unit>) {
        _actionError.value = result.exceptionOrNull()?.message
    }
}
