package com.chumakov123.casedocket.domain.usecase.confirmed

import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository

class UpdateConfirmedScheduleUseCase(
    private val repository: ConfirmedScheduleRepository
) {
    suspend operator fun invoke(schedule: CourtSchedule) {
        repository.updateSchedule(schedule)
    }
}