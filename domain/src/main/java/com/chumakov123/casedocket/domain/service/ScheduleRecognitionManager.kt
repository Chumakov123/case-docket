package com.chumakov123.casedocket.domain.service

import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

class ScheduleRecognitionManager(
    private val repository: RecognitionTaskRepository,
    private val serviceController: RecognitionServiceController
) {
    suspend fun submitImage(imageUri: String) {
        repository.addTask(imageUri)
        serviceController.ensureServiceRunning()
    }

    suspend fun processNextTask(): RecognitionTask? {
        val task = repository.getNextPendingTask() ?: return null
        repository.updateTask(task.copy(status = TaskStatus.PROCESSING))
        return task
    }

    suspend fun completeTask(taskId: Long, draft: CourtScheduleDraft) {
        val task = findTask(taskId) ?: return
        repository.updateTask(
            task.copy(
                status = TaskStatus.COMPLETED,
                resultDraft = draft,
                completedAt = Date()
            )
        )
        serviceController.stopIfQueueEmpty()
    }

    suspend fun failTask(taskId: Long, error: String) {
        val task = findTask(taskId) ?: return
        repository.updateTask(
            task.copy(
                status = TaskStatus.FAILED,
                errorMessage = error,
                completedAt = Date()
            )
        )
        serviceController.stopIfQueueEmpty()
    }

    suspend fun repairStuckTasks() {
        val all = repository.observeTasks().first()
        val stuck = all.filter { it.status == TaskStatus.PROCESSING }
        stuck.forEach { repository.updateTask(it.copy(status = TaskStatus.PENDING)) }
    }

    suspend fun hasPendingTask(): Boolean {
        return repository.getNextPendingTask() != null
    }

    fun observeTasks(): Flow<List<RecognitionTask>> = repository.observeTasks()

    private suspend fun findTask(taskId: Long): RecognitionTask? {
        return repository.observeTasks().first().find { it.id == taskId }
    }
}