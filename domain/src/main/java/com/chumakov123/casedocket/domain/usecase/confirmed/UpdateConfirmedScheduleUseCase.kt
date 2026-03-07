package com.chumakov123.casedocket.domain.usecase.confirmed

import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase

class UpdateConfirmedScheduleUseCase(
    private val repository: ConfirmedScheduleRepository,
    private val rescheduleNotificationsUseCase: RescheduleNotificationsUseCase
) {
    suspend operator fun invoke(schedule: CourtSchedule) {
        repository.updateSchedule(schedule)
        rescheduleNotificationsUseCase()
    }
}