package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.agnov.ameli.data.AccountPreferencesStore
import cl.agnov.ameli.data.CredentialStore
import cl.agnov.ameli.data.NetworkProfilesRepository
import cl.agnov.ameli.sip.AccountConfigurator
import cl.agnov.ameli.sip.model.AudioCodec
import cl.agnov.ameli.sip.model.NetworkProfile
import cl.agnov.ameli.sip.model.SipAccountConfig
import cl.agnov.ameli.sip.model.SipRegistrationState
import cl.agnov.ameli.sip.model.SipTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
    val iceEnabled: Boolean = false,
    val turnEnabled: Boolean = false,
    val turnServer: String = "",
    val turnUsername: String = "",
    val turnPassword: String = "",
    val codecPriority: List<AudioCodec> = AudioCodec.DEFAULT_PRIORITY,
    val agcEnabled: Boolean = true,
    val noiseSuppressionEnabled: Boolean = true,
    val echoCancellationEnabled: Boolean = true,
    val micGainDb: Float = 0f,
    val playbackGainDb: Float = 0f,
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
    private val networkProfilesRepository: NetworkProfilesRepository,
    registrationState: StateFlow<SipRegistrationState>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val registrationState: StateFlow<SipRegistrationState> = registrationState

    val networkProfiles: StateFlow<List<NetworkProfile>> = networkProfilesRepository.profiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val savedPreferences = preferencesRepository.accountPreferences.first()
            val savedPassword = secureCredentialStore.readPassword()
            val savedTurnPassword = secureCredentialStore.readTurnPassword()
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
                    iceEnabled = savedPreferences.iceEnabled,
                    turnEnabled = savedPreferences.turnEnabled,
                    turnServer = savedPreferences.turnServer,
                    turnUsername = savedPreferences.turnUsername,
                    turnPassword = savedTurnPassword.orEmpty(),
                    codecPriority = savedPreferences.codecPriority,
                    agcEnabled = savedPreferences.agcEnabled,
                    noiseSuppressionEnabled = savedPreferences.noiseSuppressionEnabled,
                    echoCancellationEnabled = savedPreferences.echoCancellationEnabled,
                    micGainDb = savedPreferences.micGainDb,
                    playbackGainDb = savedPreferences.playbackGainDb,
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

    fun onIceEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(iceEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onTurnEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(turnEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onTurnServerChanged(value: String) {
        _uiState.value = _uiState.value.copy(turnServer = value, saveError = null, saveSucceeded = false)
    }

    fun onTurnUsernameChanged(value: String) {
        _uiState.value = _uiState.value.copy(turnUsername = value, saveError = null, saveSucceeded = false)
    }

    fun onTurnPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(turnPassword = value, saveError = null, saveSucceeded = false)
    }

    fun onCodecToggled(codec: AudioCodec, enabled: Boolean) {
        val current = _uiState.value.codecPriority
        val updated = if (enabled) {
            if (codec in current) current else current + codec
        } else {
            current - codec
        }
        _uiState.value = _uiState.value.copy(codecPriority = updated, saveError = null, saveSucceeded = false)
    }

    fun onCodecMoved(codec: AudioCodec, delta: Int) {
        val current = _uiState.value.codecPriority.toMutableList()
        val index = current.indexOf(codec)
        val newIndex = (index + delta).coerceIn(0, current.lastIndex)
        if (index == -1 || index == newIndex) return
        current.removeAt(index)
        current.add(newIndex, codec)
        _uiState.value = _uiState.value.copy(codecPriority = current, saveError = null, saveSucceeded = false)
    }

    fun onAgcEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(agcEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onNoiseSuppressionEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(noiseSuppressionEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onEchoCancellationEnabledChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(echoCancellationEnabled = value, saveError = null, saveSucceeded = false)
    }

    fun onMicGainDbChanged(value: Float) {
        _uiState.value = _uiState.value.copy(micGainDb = value, saveError = null, saveSucceeded = false)
    }

    fun onPlaybackGainDbChanged(value: Float) {
        _uiState.value = _uiState.value.copy(playbackGainDb = value, saveError = null, saveSucceeded = false)
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
            iceEnabled = state.iceEnabled,
            turnEnabled = state.turnEnabled,
            turnServer = state.turnServer.trim(),
            turnUsername = state.turnUsername.trim(),
            turnPassword = state.turnPassword,
            codecPriority = state.codecPriority,
            agcEnabled = state.agcEnabled,
            noiseSuppressionEnabled = state.noiseSuppressionEnabled,
            echoCancellationEnabled = state.echoCancellationEnabled,
            micGainDb = state.micGainDb,
            playbackGainDb = state.playbackGainDb,
        )

        _uiState.value = state.copy(isSaving = true, saveError = null, saveSucceeded = false)

        viewModelScope.launch {
            preferencesRepository.saveAccountPreferences(config.toPreferences())
            secureCredentialStore.savePassword(config.password)
            secureCredentialStore.saveTurnPassword(config.turnPassword)

            val result = sipAccountManager.applyAccount(config)
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                saveSucceeded = result.isSuccess,
                saveError = result.exceptionOrNull()?.message,
            )
        }
    }

    /** Guarda la configuración actual de NAT/códec (sin credenciales) como un perfil de red reutilizable. */
    fun saveCurrentAsNetworkProfile(name: String) {
        val state = _uiState.value
        val profile = NetworkProfile(
            name = name,
            stunEnabled = state.stunEnabled,
            stunServer = state.stunServer.trim(),
            iceEnabled = state.iceEnabled,
            turnEnabled = state.turnEnabled,
            turnServer = state.turnServer.trim(),
            codecPriority = state.codecPriority,
        )
        viewModelScope.launch { networkProfilesRepository.save(profile) }
    }

    /** Aplica un perfil de red guardado (NAT/códec) y lo persiste/activa de inmediato contra la cuenta SIP. */
    fun applyNetworkProfile(profile: NetworkProfile) {
        _uiState.value = _uiState.value.copy(
            stunEnabled = profile.stunEnabled,
            stunServer = profile.stunServer,
            iceEnabled = profile.iceEnabled,
            turnEnabled = profile.turnEnabled,
            turnServer = profile.turnServer,
            codecPriority = profile.codecPriority,
            saveError = null,
            saveSucceeded = false,
        )
        save()
    }

    fun deleteNetworkProfile(profile: NetworkProfile) {
        viewModelScope.launch { networkProfilesRepository.remove(profile) }
    }
}
