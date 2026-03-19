package com.chumakov123.casedocket.domain.usecase.task

import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager

class RetryTaskUseCase(
    private val manager: ScheduleRecognitionManager,
    private val repository: RecognitionTaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        val task = repository.getTaskById(taskId) ?: return
        manager.submitImage(task.imageUri)
        repository.deleteTask(taskId)
    }
}