package com.chumakov123.casedocket.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.chumakov123.casedocket.data.local.entity.ConfirmedScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfirmedScheduleDao {
    @Insert
    suspend fun insert(schedule: ConfirmedScheduleEntity): Long

    @Update
    suspend fun update(schedule: ConfirmedScheduleEntity)

    @Delete
    suspend fun delete(schedule: ConfirmedScheduleEntity)

    @Query("SELECT * FROM confirmed_schedules ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ConfirmedScheduleEntity>>

    @Query("SELECT * FROM confirmed_schedules WHERE id = :id")
    suspend fun getById(id: Long): ConfirmedScheduleEntity?
}