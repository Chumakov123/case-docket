package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.data.dto.CourtScheduleDraftDto
import com.chumakov123.casedocket.data.local.dao.RecognitionTaskDao
import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity
import com.chumakov123.casedocket.data.mapper.toDomain
import com.chumakov123.casedocket.data.mapper.toDto
import com.chumakov123.casedocket.data.mapper.toEntity
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class RecognitionTaskRepositoryImpl(
    private val dao: RecognitionTaskDao,
    private val json: Json
) : RecognitionTaskRepository {
    override fun observeTasks(): Flow<List<RecognitionTask>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTask(imageUri: String): Long {
        val entity = RecognitionTaskEntity(
            imageUri = imageUri,
            status = TaskStatus.PENDING.name,
            resultDraftJson = null,
            errorMessage = null,
            createdAt = System.currentTimeMillis(),
            completedAt = null
        )
        return dao.insert(entity)
    }

    override suspend fun updateTask(task: RecognitionTask) {
        dao.update(task.toEntity())
    }

    override suspend fun getNextPendingTask(): RecognitionTask? {
        return dao.getNextPending()?.toDomain()
    }

    override suspend fun getPendingCount(): Int = dao.getPendingCount()

    override suspend fun deleteTask(id: Long) {
        val entity = dao.getById(id)
        if (entity != null) dao.delete(entity)
    }

    override suspend fun getDraftByTaskId(taskId: Long): CourtScheduleDraft? {
        val entity = dao.getById(taskId) ?: return null
        val jsonString = entity.resultDraftJson ?: return null
        return try {
            val dto = json.decodeFromString<CourtScheduleDraftDto>(jsonString)
            dto.toDomain()
        } catch (e: SerializationException) {
            null
        }
    }

    override suspend fun updateDraft(taskId: Long, draft: CourtScheduleDraft) {
        val entity = dao.getById(taskId) ?: return
        val dto = draft.toDto()
        val jsonString = json.encodeToString(dto)
        val updatedEntity = entity.copy(resultDraftJson = jsonString)
        dao.update(updatedEntity)
    }
}