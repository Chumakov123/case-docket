package com.chumakov123.casedocket.domain.model

import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import java.util.Date

enum class TaskStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

data class RecognitionTask(
    val id: Long = 0,
    val imageUri: String,
    val status: TaskStatus,
    val errorMessage: String? = null,
    val createdAt: Date = Date(),
    val completedAt: Date? = null,
    val resultDraft: CourtScheduleDraft? = null
)