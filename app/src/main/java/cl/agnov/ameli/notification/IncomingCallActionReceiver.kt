package cl.agnov.ameli.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import cl.agnov.ameli.AmeliApplication

/**
 * Recibe las acciones "Contestar"/"Rechazar" pulsadas directamente desde la
 * notificación de llamada entrante, sin necesidad de abrir la app.
 */
class IncomingCallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callManager = (context.applicationContext as AmeliApplication).container.callManager
        when (intent.action) {
            ACTION_ANSWER_CALL -> callManager.answer()
            ACTION_DECLINE_CALL -> callManager.decline()
            else -> return
        }
        NotificationManagerCompat.from(context).cancel(INCOMING_CALL_NOTIFICATION_ID)
    }
}
