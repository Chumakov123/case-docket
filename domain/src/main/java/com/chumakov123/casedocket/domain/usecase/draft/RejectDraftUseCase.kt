package com.chumakov123.casedocket.domain.usecase.draft

import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository

class RejectDraftUseCase(
    private val taskRepository: RecognitionTaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        taskRepository.deleteTask(taskId)
    }
}