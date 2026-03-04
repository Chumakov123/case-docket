package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import kotlinx.coroutines.flow.Flow

interface ConfirmedScheduleRepository {
    suspend fun addSchedule(schedule: CourtSchedule): Long
    suspend fun updateSchedule(id: Long, schedule: CourtSchedule)
    suspend fun deleteSchedule(id: Long)
    fun observeAllSchedules(): Flow<List<CourtSchedule>>
    suspend fun getScheduleById(id: Long): CourtSchedule?
}