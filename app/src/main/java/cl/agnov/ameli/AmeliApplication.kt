package cl.agnov.ameli

import android.app.Application
import cl.agnov.ameli.sip.LinphoneManager

class AmeliApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Temporal: a partir de la Fase 7, el ciclo de vida del Core pasa a
        // ser responsabilidad exclusiva de LinphoneService.
        LinphoneManager.start(this)
    }
}
