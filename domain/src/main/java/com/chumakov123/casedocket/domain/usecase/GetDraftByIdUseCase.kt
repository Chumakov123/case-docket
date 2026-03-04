package com.chumakov123.casedocket.domain.usecase

import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository

class GetDraftByIdUseCase(
    private val repository: RecognitionTaskRepository
) {
    suspend operator fun invoke(taskId: Long): CourtScheduleDraft? {
        return repository.getDraftByTaskId(taskId)
    }
}