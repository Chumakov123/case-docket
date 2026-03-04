package com.chumakov123.casedocket.domain.usecase

import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository

class ConfirmDraftUseCase(
    private val taskRepository: RecognitionTaskRepository,
    private val confirmedRepository: ConfirmedScheduleRepository
) {
    suspend operator fun invoke(taskId: Long, confirmedSchedule: CourtSchedule) {
        confirmedRepository.addSchedule(confirmedSchedule)
        taskRepository.deleteTask(taskId)
    }
}