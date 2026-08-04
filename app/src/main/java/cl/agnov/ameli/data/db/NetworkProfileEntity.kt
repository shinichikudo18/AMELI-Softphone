package cl.agnov.ameli.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_profiles")
data class NetworkProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val stunEnabled: Boolean,
    val stunServer: String,
    val iceEnabled: Boolean,
    val turnEnabled: Boolean,
    val turnServer: String,
    val codecPriority: String,
)
