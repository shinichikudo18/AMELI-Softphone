package cl.agnov.ameli.sip.model

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
}

data class CallUiState(
    val direction: CallDirection,
    val remoteAddress: String,
    val remoteDisplayName: String?,
    val connectionState: CallConnectionState,
    val durationSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
)
