package com.chumakov123.casedocket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recognition_tasks")
data class RecognitionTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val status: String,
    val resultDraftJson: String?,
    val errorMessage: String?,
    val createdAt: Long,
    val completedAt: Long? = null
)