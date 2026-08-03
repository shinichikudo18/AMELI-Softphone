package cl.agnov.ameli

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import cl.agnov.ameli.notification.CallNotificationController
import cl.agnov.ameli.service.LinphoneService

class AmeliApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CallNotificationController(this, container.callManager.callState)
        ContextCompat.startForegroundService(this, Intent(this, LinphoneService::class.java))
    }
}
