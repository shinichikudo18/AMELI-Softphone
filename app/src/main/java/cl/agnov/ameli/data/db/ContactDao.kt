package cl.agnov.ameli.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(entity: ContactEntity)

    @Delete
    suspend fun delete(entity: ContactEntity)

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name ASC")
    fun observeAll(): Flow<List<ContactEntity>>
}
