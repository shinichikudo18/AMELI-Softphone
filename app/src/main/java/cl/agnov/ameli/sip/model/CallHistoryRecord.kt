package cl.agnov.ameli.sip.model

import org.linphone.core.Call

enum class CallResult {
    SUCCESS,
    MISSED,
    DECLINED,
    ABORTED,
    ;

    companion object {
        fun from(status: Call.Status): CallResult = when (status) {
            Call.Status.Success -> SUCCESS
            Call.Status.Missed -> MISSED
            Call.Status.Declined, Call.Status.DeclinedElsewhere -> DECLINED
            Call.Status.Aborted,
            Call.Status.EarlyAborted,
            Call.Status.AcceptedElsewhere,
            -> ABORTED
        }
    }
}

data class CallHistoryRecord(
    val id: Long = 0,
    val remoteAddress: String,
    val remoteDisplayName: String?,
    val direction: CallDirection,
    val startDateEpochSeconds: Long,
    val durationSeconds: Int,
    val result: CallResult,
)
