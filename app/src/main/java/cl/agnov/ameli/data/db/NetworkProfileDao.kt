package cl.agnov.ameli.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkProfileDao {
    @Insert
    suspend fun insert(entity: NetworkProfileEntity)

    @Delete
    suspend fun delete(entity: NetworkProfileEntity)

    @Query("SELECT * FROM network_profiles ORDER BY name ASC")
    fun observeAll(): Flow<List<NetworkProfileEntity>>
}
