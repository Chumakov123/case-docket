package com.chumakov123.casedocket.data.repository

import com.chumakov123.casedocket.data.local.dao.RecognitionTaskDao
import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity
import com.chumakov123.casedocket.data.mapper.toDomain
import com.chumakov123.casedocket.data.mapper.toEntity
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.TaskStatus
import com.chumakov123.casedocket.domain.repository.RecognitionTaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RecognitionTaskRepositoryImpl(
    private val dao: RecognitionTaskDao,
    private val json: Json
) : RecognitionTaskRepository {
    override fun observeTasks(): Flow<List<RecognitionTask>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain(json) } }

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
        dao.update(task.toEntity(json = json))
    }

    override suspend fun getNextPendingTask(): RecognitionTask? {
        return dao.getNextPending()?.toDomain(json = json)
    }

    override suspend fun getPendingCount(): Int = dao.getPendingCount()

    override suspend fun deleteTask(id: Long) {
        val entity = dao.getById(id)
        if (entity != null) dao.delete(entity)
    }

    override suspend fun getTaskById(id: Long): RecognitionTask? {
        return dao.getById(id)?.toDomain(json)
    }
}