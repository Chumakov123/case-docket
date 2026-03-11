package com.chumakov123.casedocket.domain.usecase.draft

import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository

class UpdateDraftUseCase(
    private val repository: RecognitionTaskRepository
) {
    suspend operator fun invoke(taskId: Long, draft: CourtScheduleDraft) {
        val task = repository.getTaskById(taskId) ?: return
        val updatedTask = task.copy(resultDraft = draft)
        repository.updateTask(updatedTask)
    }
}