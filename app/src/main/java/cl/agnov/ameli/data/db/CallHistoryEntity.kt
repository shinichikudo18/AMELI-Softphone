package cl.agnov.ameli.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_history")
data class CallHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteAddress: String,
    val remoteDisplayName: String?,
    val direction: String,
    val startDateEpochSeconds: Long,
    val durationSeconds: Int,
    val result: String,
)
