package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.ThemeMode
import cl.agnov.ameli.data.ThemePreferenceStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Expone la preferencia de tema (sistema/claro/oscuro) y permite cambiarla. */
class ThemeViewModel(private val themePreferenceStore: ThemePreferenceStore) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferenceStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themePreferenceStore.setThemeMode(mode) }
    }
}
