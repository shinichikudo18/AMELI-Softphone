package cl.agnov.ameli.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.agnov.ameli.AmeliApplication
import cl.agnov.ameli.sip.LinphoneManager

object ViewModelFactories {

    val settings = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AmeliApplication
            SettingsViewModel(
                preferencesRepository = app.container.preferencesRepository,
                secureCredentialStore = app.container.secureCredentialStore,
                sipAccountManager = app.container.sipAccountManager,
                registrationState = LinphoneManager.registrationState,
            )
        }
    }

    val dialer = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AmeliApplication
            DialerViewModel(
                callController = app.container.callManager,
                registrationState = LinphoneManager.registrationState,
            )
        }
    }

    val activeCall = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AmeliApplication
            ActiveCallViewModel(callController = app.container.callManager)
        }
    }

    val incomingCall = viewModelFactory {
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AmeliApplication
            IncomingCallViewModel(callController = app.container.callManager)
        }
    }
}
