package cl.agnov.ameli.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.agnov.ameli.MainActivity
import cl.agnov.ameli.R
import cl.agnov.ameli.sip.model.CallConnectionState
import cl.agnov.ameli.sip.model.CallDirection
import cl.agnov.ameli.sip.model.CallUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val INCOMING_CALL_CHANNEL_ID = "incoming_call"
const val INCOMING_CALL_NOTIFICATION_ID = 1001
const val ACTION_ANSWER_CALL = "cl.agnov.ameli.action.ANSWER_CALL"
const val ACTION_DECLINE_CALL = "cl.agnov.ameli.action.DECLINE_CALL"

/**
 * Observa el estado de la llamada y muestra/oculta la notificación de
 * llamada entrante (categoría CALL, con pantalla completa para desbloquear
 * el dispositivo y acciones para contestar/rechazar directamente).
 */
class CallNotificationController(
    private val context: Context,
    callState: StateFlow<CallUiState?>,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
        scope.launch {
            var wasRinging = false
            callState.collect { state ->
                val isRinging = state != null &&
                    state.direction == CallDirection.INCOMING &&
                    state.connectionState == CallConnectionState.INCOMING_RINGING

                if (isRinging && !wasRinging) {
                    showIncomingCallNotification(state)
                } else if (!isRinging && wasRinging) {
                    dismissIncomingCallNotification()
                }
                wasRinging = isRinging
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            INCOMING_CALL_CHANNEL_ID,
            context.getString(R.string.notification_channel_incoming_call),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_incoming_call_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun showIncomingCallNotification(state: CallUiState) {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            Intent(context, IncomingCallActionReceiver::class.java).setAction(ACTION_ANSWER_CALL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, IncomingCallActionReceiver::class.java).setAction(ACTION_DECLINE_CALL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, INCOMING_CALL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_call)
            .setContentTitle(context.getString(R.string.notification_incoming_call_title))
            .setContentText(state.remoteDisplayName ?: state.remoteAddress)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, context.getString(R.string.notification_action_decline), declinePendingIntent)
            .addAction(0, context.getString(R.string.notification_action_answer), answerPendingIntent)
            .build()

        val hasNotificationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (hasNotificationPermission) {
            notificationManagerCompat.notify(INCOMING_CALL_NOTIFICATION_ID, notification)
        }
    }

    private fun dismissIncomingCallNotification() {
        notificationManagerCompat.cancel(INCOMING_CALL_NOTIFICATION_ID)
    }
}
