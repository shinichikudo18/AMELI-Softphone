package cl.agnov.ameli.ui.viewmodel

import cl.agnov.ameli.data.AccountPreferencesStore
import cl.agnov.ameli.data.CredentialStore
import cl.agnov.ameli.sip.AccountConfigurator
import cl.agnov.ameli.sip.model.SipAccountConfig
import cl.agnov.ameli.sip.model.SipAccountPreferences
import cl.agnov.ameli.sip.model.SipRegistrationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeAccountPreferencesStore(
    initial: SipAccountPreferences? = null,
) : AccountPreferencesStore {
    private val state = MutableStateFlow(initial)
    override val accountPreferences = state
    var lastSaved: SipAccountPreferences? = null
        private set

    override suspend fun saveAccountPreferences(preferences: SipAccountPreferences) {
        lastSaved = preferences
        state.value = preferences
    }

    override suspend fun clearAccountPreferences() {
        state.value = null
    }
}

private class FakeCredentialStore(initialPassword: String? = null) : CredentialStore {
    private var password: String? = initialPassword
    private var turnPassword: String? = null

    override fun savePassword(password: String) {
        this.password = password
    }

    override fun readPassword(): String? = password

    override fun saveTurnPassword(password: String) {
        turnPassword = password
    }

    override fun readTurnPassword(): String? = turnPassword

    override fun clear() {
        password = null
        turnPassword = null
    }
}

private class FakeAccountConfigurator(
    private val result: Result<Unit> = Result.success(Unit),
) : AccountConfigurator {
    var lastConfig: SipAccountConfig? = null
        private set

    override fun applyAccount(config: SipAccountConfig): Result<Unit> {
        lastConfig = config
        return result
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads saved preferences and password on init`() = runTest {
        val savedPreferences = SipAccountPreferences(username = "alice", domain = "sip.example.com", port = 5061)
        val preferencesStore = FakeAccountPreferencesStore(savedPreferences)
        val credentialStore = FakeCredentialStore(initialPassword = "s3cret")

        val viewModel = SettingsViewModel(
            preferencesRepository = preferencesStore,
            secureCredentialStore = credentialStore,
            sipAccountManager = FakeAccountConfigurator(),
            registrationState = MutableStateFlow(SipRegistrationState.NOT_REGISTERED),
        )
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("alice", state.username)
        assertEquals("sip.example.com", state.domain)
        assertEquals("5061", state.port)
        assertEquals("s3cret", state.password)
    }

    @Test
    fun `save fails validation when required fields are blank`() = runTest {
        val viewModel = SettingsViewModel(
            preferencesRepository = FakeAccountPreferencesStore(),
            secureCredentialStore = FakeCredentialStore(),
            sipAccountManager = FakeAccountConfigurator(),
            registrationState = MutableStateFlow(SipRegistrationState.NOT_REGISTERED),
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.saveSucceeded)
        assertTrue(viewModel.uiState.value.saveError != null)
    }

    @Test
    fun `save persists preferences, password and applies account`() = runTest {
        val preferencesStore = FakeAccountPreferencesStore()
        val credentialStore = FakeCredentialStore()
        val accountConfigurator = FakeAccountConfigurator()

        val viewModel = SettingsViewModel(
            preferencesRepository = preferencesStore,
            secureCredentialStore = credentialStore,
            sipAccountManager = accountConfigurator,
            registrationState = MutableStateFlow(SipRegistrationState.NOT_REGISTERED),
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onUsernameChanged("bob")
        viewModel.onDomainChanged("sip.example.com")
        viewModel.onPasswordChanged("hunter2")
        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveSucceeded)
        assertNull(viewModel.uiState.value.saveError)
        assertEquals("bob", preferencesStore.lastSaved?.username)
        assertEquals("hunter2", credentialStore.readPassword())
        assertEquals("bob", accountConfigurator.lastConfig?.username)
        assertEquals("hunter2", accountConfigurator.lastConfig?.password)
    }
}
