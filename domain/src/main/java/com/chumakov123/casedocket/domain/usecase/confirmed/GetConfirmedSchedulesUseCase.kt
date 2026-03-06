package com.chumakov123.casedocket.domain.usecase.confirmed

import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class GetConfirmedSchedulesUseCase(
    private val repository: ConfirmedScheduleRepository
) {
    operator fun invoke(now: LocalDateTime): Flow<Pair<List<CourtSchedule>, List<CourtSchedule>>> {
        return repository.observeAllSchedules().map { schedules ->
            val (active, archived) = schedules.partition { it.hasFutureCases(now) }
            val sortedActive = active.sortedBy { schedule ->
                schedule.getEarliestFutureCaseTime(now) ?: LocalDateTime.MAX
            }

            val sortedArchived = archived.sortedByDescending { it.date.value }
            Pair(sortedActive, sortedArchived)
        }
    }
}