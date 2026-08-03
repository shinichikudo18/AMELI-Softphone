package cl.agnov.ameli.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CallHistoryEntity::class], version = 1, exportSchema = false)
abstract class AmeliDatabase : RoomDatabase() {
    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        @Volatile
        private var instance: AmeliDatabase? = null

        fun getInstance(context: Context): AmeliDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AmeliDatabase::class.java,
                    "ameli.db",
                ).build().also { instance = it }
            }
    }
}
