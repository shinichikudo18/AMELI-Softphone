package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.CallHistoryRepository
import cl.agnov.ameli.sip.model.CallHistoryRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Expone el historial local de llamadas. */
class HistoryViewModel(
    private val repository: CallHistoryRepository,
) : ViewModel() {

    val history: StateFlow<List<CallHistoryRecord>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clear() {
        viewModelScope.launch { repository.clear() }
    }
}
