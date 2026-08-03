package cl.agnov.ameli

import android.content.Context
import cl.agnov.ameli.data.PreferencesRepository
import cl.agnov.ameli.data.SecureCredentialStore
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
}
