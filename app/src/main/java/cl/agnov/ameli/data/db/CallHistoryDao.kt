package cl.agnov.ameli.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Insert
    suspend fun insert(entity: CallHistoryEntity)

    @Query("SELECT * FROM call_history ORDER BY startDateEpochSeconds DESC")
    fun observeAll(): Flow<List<CallHistoryEntity>>

    @Query("DELETE FROM call_history")
    suspend fun clear()
}
