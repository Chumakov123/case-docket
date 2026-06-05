package com.chumakov123.casedocket.data.alarm

interface NotifiedAlarmStore {
    suspend fun markAsNotified(requestCode: Int)
    suspend fun isNotified(requestCode: Int): Boolean
    suspend fun clearOld(thresholdMillis: Long)
}