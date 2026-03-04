package com.chumakov123.casedocket.domain.model

import java.util.Date

enum class TaskStatus {
    PENDING, PROCESSING, COMPLETED, FAILED
}

data class RecognitionTask(
    val id: Long = 0,
    val imageUri: String,
    val status: TaskStatus,
    val resultDraftJson: String? = null,
    val errorMessage: String? = null,
    val createdAt: Date = Date(),
    val completedAt: Date? = null
)