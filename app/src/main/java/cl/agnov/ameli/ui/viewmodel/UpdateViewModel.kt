package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.update.ReleaseInfo
import cl.agnov.ameli.update.UpdateChecker
import cl.agnov.ameli.update.UpdateDismissalStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Consulta si hay una versión más nueva publicada en GitHub Releases al abrir la app. */
class UpdateViewModel(
    private val updateChecker: UpdateChecker,
    private val dismissalStore: UpdateDismissalStore,
) : ViewModel() {

    private val _availableUpdate = MutableStateFlow<ReleaseInfo?>(null)
    val availableUpdate: StateFlow<ReleaseInfo?> = _availableUpdate.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val release = updateChecker.checkForUpdate() ?: return@launch
            if (!dismissalStore.isDismissed(release.versionName)) {
                _availableUpdate.value = release
            }
        }
    }

    fun dismiss() {
        _availableUpdate.value?.let { dismissalStore.dismiss(it.versionName) }
        _availableUpdate.value = null
    }
}
