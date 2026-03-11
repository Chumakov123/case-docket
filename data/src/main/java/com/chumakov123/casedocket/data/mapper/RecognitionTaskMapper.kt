package com.chumakov123.casedocket.data.mapper

import com.chumakov123.casedocket.data.dto.CourtScheduleDraftDto
import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.util.Date

fun RecognitionTaskEntity.toDomain(json: Json): RecognitionTask {
    val draft = resultDraftJson?.let { jsonString ->
        try {
            val dto = json.decodeFromString<CourtScheduleDraftDto>(jsonString)
            dto.toDomain()
        } catch (e: SerializationException) {
            null
        }
    }
    return RecognitionTask(
        id = id,
        imageUri = imageUri,
        status = TaskStatus.valueOf(status),
        errorMessage = errorMessage,
        createdAt = Date(createdAt),
        completedAt = completedAt?.let { Date(it) },
        resultDraft = draft
    )
}

fun RecognitionTask.toEntity(json: Json): RecognitionTaskEntity = RecognitionTaskEntity(
    id = id,
    imageUri = imageUri,
    status = status.name,
    resultDraftJson = resultDraft?.let { draft ->
        json.encodeToString(draft.toDto())
    },
    errorMessage = errorMessage,
    createdAt = createdAt.time,
    completedAt = completedAt?.time
)