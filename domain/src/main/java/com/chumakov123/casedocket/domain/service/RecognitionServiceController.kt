package com.chumakov123.casedocket.domain.service

interface RecognitionServiceController {
    suspend fun ensureServiceRunning()
    suspend fun stopIfQueueEmpty()
}