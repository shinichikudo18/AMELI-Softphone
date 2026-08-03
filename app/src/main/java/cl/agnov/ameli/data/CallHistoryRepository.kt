package cl.agnov.ameli.data

import cl.agnov.ameli.data.db.CallHistoryDao
import cl.agnov.ameli.data.db.CallHistoryEntity
import cl.agnov.ameli.sip.model.CallDirection
import cl.agnov.ameli.sip.model.CallHistoryRecord
import cl.agnov.ameli.sip.model.CallResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Permite sustituir [RoomCallHistoryRepository] por un fake en pruebas unitarias. */
interface CallHistoryRepository {
    val history: Flow<List<CallHistoryRecord>>
    suspend fun record(entry: CallHistoryRecord)
    suspend fun clear()
}

/** Historial local de llamadas persistido con Room. */
class RoomCallHistoryRepository(private val dao: CallHistoryDao) : CallHistoryRepository {

    override val history: Flow<List<CallHistoryRecord>> =
        dao.observeAll().map { entities -> entities.map { it.toRecord() } }

    override suspend fun record(entry: CallHistoryRecord) {
        dao.insert(entry.toEntity())
    }

    override suspend fun clear() {
        dao.clear()
    }

    private fun CallHistoryEntity.toRecord() = CallHistoryRecord(
        id = id,
        remoteAddress = remoteAddress,
        remoteDisplayName = remoteDisplayName,
        direction = CallDirection.valueOf(direction),
        startDateEpochSeconds = startDateEpochSeconds,
        durationSeconds = durationSeconds,
        result = CallResult.valueOf(result),
    )

    private fun CallHistoryRecord.toEntity() = CallHistoryEntity(
        id = id,
        remoteAddress = remoteAddress,
        remoteDisplayName = remoteDisplayName,
        direction = direction.name,
        startDateEpochSeconds = startDateEpochSeconds,
        durationSeconds = durationSeconds,
        result = result.name,
    )
}
