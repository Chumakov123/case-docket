package com.chumakov123.casedocket.data.alarm

interface ActiveAlarmStore {
    suspend fun addRequestCode(requestCode: Int)
    suspend fun getAllRequestCodes(): List<Int>
    suspend fun clearAll()
}