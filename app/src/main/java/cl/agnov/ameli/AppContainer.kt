package cl.agnov.ameli

import android.content.Context
import cl.agnov.ameli.data.CallHistoryRepository
import cl.agnov.ameli.data.PreferencesRepository
import cl.agnov.ameli.data.RoomCallHistoryRepository
import cl.agnov.ameli.data.SecureCredentialStore
import cl.agnov.ameli.data.db.AmeliDatabase
import cl.agnov.ameli.sip.AudioRouteManager
import cl.agnov.ameli.sip.CallManager
import cl.agnov.ameli.sip.SipAccountManager

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
    val callManager = CallManager(AudioRouteManager(), callHistoryRepository)
}
