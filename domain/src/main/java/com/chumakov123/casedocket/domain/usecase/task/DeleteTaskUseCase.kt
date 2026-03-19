package com.chumakov123.casedocket.domain.usecase.task

import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository

class DeleteTaskUseCase(private val repository: RecognitionTaskRepository) {
    suspend operator fun invoke(taskId: Long) = repository.deleteTask(taskId)
}