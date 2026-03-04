package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import kotlinx.coroutines.flow.Flow

interface RecognitionTaskRepository {
    fun observeTasks(): Flow<List<RecognitionTask>>
    suspend fun addTask(imageUri: String): Long
    suspend fun updateTask(task: RecognitionTask)
    suspend fun getNextPendingTask(): RecognitionTask?
    suspend fun getPendingCount(): Int
    suspend fun deleteTask(id: Long)
    suspend fun getDraftByTaskId(taskId: Long): CourtScheduleDraft?
    suspend fun updateDraft(taskId: Long, draft: CourtScheduleDraft)
}