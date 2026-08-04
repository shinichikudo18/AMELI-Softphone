package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.CallHistoryRepository
import cl.agnov.ameli.sip.CallController
import cl.agnov.ameli.sip.model.CallHistoryRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Expone el historial local de llamadas y permite volver a llamar desde ahí. */
class HistoryViewModel(
    private val repository: CallHistoryRepository,
    private val callController: CallController,
) : ViewModel() {

    val history: StateFlow<List<CallHistoryRecord>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clear() {
        viewModelScope.launch { repository.clear() }
    }

    fun redial(record: CallHistoryRecord): Result<Unit> = callController.call(record.remoteAddress)
}
