package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.RecognitionTask
import kotlinx.coroutines.flow.Flow

interface RecognitionTaskRepository {
    fun observeTasks(): Flow<List<RecognitionTask>>
    suspend fun addTask(imageUri: String): Long
    suspend fun updateTask(task: RecognitionTask)
    suspend fun getNextPendingTask(): RecognitionTask?
    suspend fun getPendingCount(): Int
    suspend fun deleteTask(id: Long)
    suspend fun getTaskById(id: Long): RecognitionTask?
}