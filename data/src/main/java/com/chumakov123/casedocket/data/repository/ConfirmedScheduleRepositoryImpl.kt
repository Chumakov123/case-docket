package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.data.local.dao.ConfirmedScheduleDao
import com.chumakov123.casedocket.data.mapper.toConfirmedScheduleEntity
import com.chumakov123.casedocket.data.mapper.toCourtSchedule
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ConfirmedScheduleRepositoryImpl(
    private val dao: ConfirmedScheduleDao,
    private val json: Json
) : ConfirmedScheduleRepository {

    override suspend fun addSchedule(schedule: CourtSchedule): Long {
        val entity = schedule.toConfirmedScheduleEntity(json)
        return dao.insert(entity)
    }

    override suspend fun updateSchedule(id: Long, schedule: CourtSchedule) {
        val entity = schedule.toConfirmedScheduleEntity(json, id)
        dao.update(entity)
    }

    override suspend fun deleteSchedule(id: Long) {
        dao.getById(id)?.let { dao.delete(it) }
    }

    override fun observeAllSchedules(): Flow<List<CourtSchedule>> =
        dao.observeAll().map { entities ->
            entities.mapNotNull { it.toCourtSchedule(json) }
        }

    override suspend fun getScheduleById(id: Long): CourtSchedule? =
        dao.getById(id)?.toCourtSchedule(json)
}