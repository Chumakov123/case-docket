package com.chumakov123.casedocket.domain.usecase.confirmed

import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase

class DeleteConfirmedScheduleUseCase(
    private val repository: ConfirmedScheduleRepository,
    private val rescheduleNotificationsUseCase: RescheduleNotificationsUseCase
) {
    suspend operator fun invoke(scheduleId: Long) {
        repository.deleteSchedule(scheduleId)
        rescheduleNotificationsUseCase()
    }
}