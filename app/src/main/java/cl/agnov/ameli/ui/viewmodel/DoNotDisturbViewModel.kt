package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.DoNotDisturbState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Controla el modo No Molestar (rechaza automáticamente las llamadas entrantes). */
class DoNotDisturbViewModel(
    private val doNotDisturbState: DoNotDisturbState,
) : ViewModel() {

    val isEnabled: StateFlow<Boolean> = doNotDisturbState.isEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { doNotDisturbState.setEnabled(enabled) }
    }
}
