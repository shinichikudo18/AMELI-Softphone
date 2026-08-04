package cl.agnov.ameli.data

import cl.agnov.ameli.data.db.NetworkProfileDao
import cl.agnov.ameli.data.db.NetworkProfileEntity
import cl.agnov.ameli.sip.model.AudioCodec
import cl.agnov.ameli.sip.model.NetworkProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Permite sustituir [RoomNetworkProfilesRepository] por un fake en pruebas unitarias. */
interface NetworkProfilesRepository {
    val profiles: Flow<List<NetworkProfile>>
    suspend fun save(profile: NetworkProfile)
    suspend fun remove(profile: NetworkProfile)
}

/** Perfiles de red (NAT/códec) locales persistidos con Room. */
class RoomNetworkProfilesRepository(private val dao: NetworkProfileDao) : NetworkProfilesRepository {

    override val profiles: Flow<List<NetworkProfile>> =
        dao.observeAll().map { entities -> entities.map { it.toProfile() } }

    override suspend fun save(profile: NetworkProfile) {
        dao.insert(profile.toEntity())
    }

    override suspend fun remove(profile: NetworkProfile) {
        dao.delete(profile.toEntity())
    }

    private fun NetworkProfileEntity.toProfile() = NetworkProfile(
        id = id,
        name = name,
        stunEnabled = stunEnabled,
        stunServer = stunServer,
        iceEnabled = iceEnabled,
        turnEnabled = turnEnabled,
        turnServer = turnServer,
        codecPriority = codecPriority.split(",").mapNotNull { runCatching { AudioCodec.valueOf(it) }.getOrNull() }
            .ifEmpty { AudioCodec.DEFAULT_PRIORITY },
    )

    private fun NetworkProfile.toEntity() = NetworkProfileEntity(
        id = id,
        name = name,
        stunEnabled = stunEnabled,
        stunServer = stunServer,
        iceEnabled = iceEnabled,
        turnEnabled = turnEnabled,
        turnServer = turnServer,
        codecPriority = codecPriority.joinToString(",") { it.name },
    )
}
