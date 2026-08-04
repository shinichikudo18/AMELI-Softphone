package cl.agnov.ameli

import android.content.Context
import cl.agnov.ameli.data.CallHistoryRepository
import cl.agnov.ameli.data.ContactsRepository
import cl.agnov.ameli.data.DoNotDisturbRepository
import cl.agnov.ameli.data.DoNotDisturbState
import cl.agnov.ameli.data.NetworkProfilesRepository
import cl.agnov.ameli.data.PreferencesRepository
import cl.agnov.ameli.data.RoomCallHistoryRepository
import cl.agnov.ameli.data.RoomContactsRepository
import cl.agnov.ameli.data.RoomNetworkProfilesRepository
import cl.agnov.ameli.data.SecureCredentialStore
import cl.agnov.ameli.data.ThemePreferenceRepository
import cl.agnov.ameli.data.db.AmeliDatabase
import cl.agnov.ameli.sip.AudioRouteManager
import cl.agnov.ameli.sip.CallManager
import cl.agnov.ameli.sip.SipAccountManager
import cl.agnov.ameli.update.UpdateChecker
import cl.agnov.ameli.update.UpdateDismissalStore

/**
 * Contenedor manual de dependencias de la aplicación. Se evita un framework
 * de inyección de dependencias dado el tamaño del proyecto; los ViewModels
 * reciben estas dependencias por constructor para poder sustituirlas por
 * fakes en las pruebas unitarias.
 */
class AppContainer(context: Context) {
    val preferencesRepository = PreferencesRepository(context.applicationContext)
    val secureCredentialStore = SecureCredentialStore(context.applicationContext)
    val sipAccountManager = SipAccountManager()
    val callHistoryRepository: CallHistoryRepository =
        RoomCallHistoryRepository(AmeliDatabase.getInstance(context.applicationContext).callHistoryDao())
    val contactsRepository: ContactsRepository =
        RoomContactsRepository(AmeliDatabase.getInstance(context.applicationContext).contactDao())
    val networkProfilesRepository: NetworkProfilesRepository =
        RoomNetworkProfilesRepository(AmeliDatabase.getInstance(context.applicationContext).networkProfileDao())
    val doNotDisturbRepository: DoNotDisturbState = DoNotDisturbRepository(context.applicationContext)
    val callManager = CallManager(AudioRouteManager(context.applicationContext), callHistoryRepository, doNotDisturbRepository)
    val updateChecker = UpdateChecker(BuildConfig.VERSION_NAME)
    val updateDismissalStore = UpdateDismissalStore(context.applicationContext)
    val themePreferenceRepository = ThemePreferenceRepository(context.applicationContext)
}
