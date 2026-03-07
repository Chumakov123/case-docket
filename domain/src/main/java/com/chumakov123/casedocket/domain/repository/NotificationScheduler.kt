package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.court.CourtSchedule

interface NotificationScheduler {
    suspend fun scheduleAllNotifications(schedules: List<CourtSchedule>)
    suspend fun cancelAllNotifications()
}