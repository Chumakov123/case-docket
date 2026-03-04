package com.chumakov123.casedocket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "confirmed_schedules")
data class ConfirmedScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,          // "dd.MM.yyyy"
    val judge: String,
    val casesJson: String,     // List<CourtCase>
    val createdAt: Long
)