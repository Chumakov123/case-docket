package com.chumakov123.casedocket.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.chumakov123.casedocket.data.local.entity.RecognitionTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecognitionTaskDao {
    @Query("SELECT * FROM recognition_tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RecognitionTaskEntity>>

    @Query("SELECT * FROM recognition_tasks WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextPending(): RecognitionTaskEntity?

    @Query("SELECT * FROM recognition_tasks WHERE id = :id")
    suspend fun getById(id: Long): RecognitionTaskEntity?

    @Query("SELECT COUNT(*) FROM recognition_tasks WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Insert
    suspend fun insert(task: RecognitionTaskEntity): Long

    @Update
    suspend fun update(task: RecognitionTaskEntity)

    @Delete
    suspend fun delete(task: RecognitionTaskEntity)
}