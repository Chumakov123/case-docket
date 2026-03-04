package com.chumakov123.casedocket.data.mapper

import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import java.util.Date

fun RecognitionTaskEntity.toDomain(): RecognitionTask = RecognitionTask(
    id = id,
    imageUri = imageUri,
    status = TaskStatus.valueOf(status),
    resultDraftJson = resultDraftJson,
    errorMessage = errorMessage,
    createdAt = Date(createdAt),
    completedAt = completedAt?.let { Date(it) }
)

fun RecognitionTask.toEntity(): RecognitionTaskEntity = RecognitionTaskEntity(
    id = id,
    imageUri = imageUri,
    status = status.name,
    resultDraftJson = resultDraftJson,
    errorMessage = errorMessage,
    createdAt = createdAt.time,
    completedAt = completedAt?.time
)