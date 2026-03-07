package com.chumakov123.casedocket.domain.usecase.notification

import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.repository.NotificationScheduler
import kotlinx.coroutines.flow.first

class RescheduleNotificationsUseCase(
    private val confirmedRepository: ConfirmedScheduleRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke() {
        val schedules = confirmedRepository.observeAllSchedules().first()
        notificationScheduler.scheduleAllNotifications(schedules)
    }
}