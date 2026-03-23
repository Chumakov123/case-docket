package com.chumakov123.casedocket.domain.usecase.draft

import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase

class ConfirmDraftUseCase(
    private val taskRepository: RecognitionTaskRepository,
    private val confirmedRepository: ConfirmedScheduleRepository,
    private val rescheduleNotificationsUseCase: RescheduleNotificationsUseCase
) {
    suspend operator fun invoke(taskId: Long, confirmedSchedule: CourtSchedule) {
        if (taskRepository.getTaskById(taskId) == null) return
        confirmedRepository.addSchedule(confirmedSchedule)
        taskRepository.deleteTask(taskId)
        rescheduleNotificationsUseCase()
    }
}