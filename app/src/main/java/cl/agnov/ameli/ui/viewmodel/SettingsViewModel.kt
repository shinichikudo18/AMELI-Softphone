package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.AccountPreferencesStore
import cl.agnov.ameli.data.CredentialStore
import cl.agnov.ameli.sip.AccountConfigurator
import cl.agnov.ameli.sip.model.SipAccountConfig
import cl.agnov.ameli.sip.model.SipRegistrationState
import cl.agnov.ameli.sip.model.SipTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val username: String = "",
    val password: String = "",
    val domain: String = "",
    val port: String = "5060",
    val transport: SipTransport = SipTransport.UDP,
    val displayName: String = "",
    val srtpEnabled: Boolean = false,
    val stunEnabled: Boolean = false,
    val stunServer: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSucceeded: Boolean = false,
) {
    val portOrNull: Int?
        get() = port.toIntOrNull()

    val isFormValid: Boolean
        get() = username.isNotBlank() &&
            domain.isNotBlank() &&
            portOrNull != null &&
            portOrNull in 1..65535
}

/**
 * Gestiona la configuración de la cuenta SIP: carga los valores guardados,
 * valida el formulario y aplica los cambios contra Liblinphone a través de
 * [SipAccountManager], separando así la lógica de la UI.
 */
class SettingsViewModel(
    private val preferencesRepository: AccountPreferencesStore,
    private val secureCredentialStore: CredentialStore,
    private val sipAccountManager: AccountConfigurator,
    registrationState: StateFlow<SipRegistrationState>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val registrationState: StateFlow<SipRegistrationState> = registrationState

    init {
        viewModelScope.launch {
            val savedPreferences = preferencesRepository.accountPreferences.first()
            val savedPassword = secureCredentialStore.readPassword()
            if (savedPreferences != null) {
                _uiState.value = SettingsUiState(
                    username = savedPreferences.username,
                    password = savedPassword.orEmpty(),
                    domain = savedPreferences.domain,
                    port = savedPreferences.port.toString(),
                    transport = savedPreferences.transport,
                    displayName = savedPreferences.displayName,
                    srtpEnabled = savedPreferences.srtpEnabled,
                    stunEnabled = savedPreferences.stunEnabled,
                    stunServer = savedPreferences.stunServer,
                    isLoading = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(username = value, saveError = null, saveSucceeded = false)
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(password = value, saveError = null, saveSucceeded = false)
    }

    fun onDomainChanged(value: String) {
        _uiState.value = _uiState.value.copy(domain = value, saveError = null, saveSucceeded = false)
    }

    fun onPortChanged(value: String) {
        _uiState.value = _uiState.value.copy(port = value, saveError = null, saveSucceeded = false)
    }

    fun onTransportChanged(value: SipTransport) {
        _uiState.value = _uiState.value.copy(transport = value, saveError = null, saveSucceeded = false)
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value, saveError = null, saveSucceeded = false)
    }

    fun onSrtpEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(srtpEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onStunEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(stunEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onStunServerChanged(value: String) {
        _uiState.value = _uiState.value.copy(stunServer = value, saveError = null, saveSucceeded = false)
    }

    fun save() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.value = state.copy(saveError = "Revisa los campos obligatorios: usuario, dominio y puerto.")
            return
        }

        val config = SipAccountConfig(
            username = state.username.trim(),
            password = state.password,
            domain = state.domain.trim(),
            port = state.portOrNull ?: 5060,
            transport = state.transport,
            displayName = state.displayName.trim(),
            srtpEnabled = state.srtpEnabled,
            stunEnabled = state.stunEnabled,
            stunServer = state.stunServer.trim(),
        )

        _uiState.value = state.copy(isSaving = true, saveError = null, saveSucceeded = false)

        viewModelScope.launch {
            preferencesRepository.saveAccountPreferences(config.toPreferences())
            secureCredentialStore.savePassword(config.password)

            val result = sipAccountManager.applyAccount(config)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveSucceeded = result.isSuccess,
                saveError = result.exceptionOrNull()?.message,
            )
        }
    }
}
