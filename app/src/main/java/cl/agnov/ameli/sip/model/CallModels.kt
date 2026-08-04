package cl.agnov.ameli.sip.model

import org.linphone.core.Call

enum class CallDirection { INCOMING, OUTGOING }

enum class CallConnectionState {
    IDLE,
    OUTGOING_INIT,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    PAUSED,
    ENDED,
    ERROR,
    ;

    companion object {
        fun from(state: Call.State): CallConnectionState = when (state) {
            Call.State.Idle -> IDLE
            Call.State.OutgoingInit, Call.State.OutgoingProgress -> OUTGOING_INIT
            Call.State.OutgoingRinging, Call.State.OutgoingEarlyMedia -> OUTGOING_RINGING
            Call.State.IncomingReceived,
            Call.State.PushIncomingReceived,
            Call.State.IncomingEarlyMedia,
            -> INCOMING_RINGING
            Call.State.Connected,
            Call.State.StreamsRunning,
            Call.State.Updating,
            Call.State.UpdatedByRemote,
            Call.State.EarlyUpdating,
            Call.State.EarlyUpdatedByRemote,
            Call.State.Resuming,
            -> CONNECTED
            Call.State.Pausing, Call.State.Paused, Call.State.PausedByRemote -> PAUSED
            Call.State.End, Call.State.Released, Call.State.Referred -> ENDED
            Call.State.Error -> ERROR
        }
    }
}

data class CallUiState(
    val callId: String,
    val direction: CallDirection,
    val remoteAddress: String,
    val remoteDisplayName: String?,
    val connectionState: CallConnectionState,
    val durationSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
)
