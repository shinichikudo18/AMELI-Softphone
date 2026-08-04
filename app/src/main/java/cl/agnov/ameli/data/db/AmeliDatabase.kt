package cl.agnov.ameli.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [CallHistoryEntity::class, ContactEntity::class, NetworkProfileEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AmeliDatabase : RoomDatabase() {
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun contactDao(): ContactDao
    abstract fun networkProfileDao(): NetworkProfileDao

    companion object {
        @Volatile
        private var instance: AmeliDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS contacts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "sipAddress TEXT NOT NULL)",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE contacts ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS network_profiles (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "stunEnabled INTEGER NOT NULL, " +
                        "stunServer TEXT NOT NULL, " +
                        "iceEnabled INTEGER NOT NULL, " +
                        "turnEnabled INTEGER NOT NULL, " +
                        "turnServer TEXT NOT NULL, " +
                        "codecPriority TEXT NOT NULL)",
                )
            }
        }

        fun getInstance(context: Context): AmeliDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AmeliDatabase::class.java,
                    "ameli.db",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
            }
    }
}
