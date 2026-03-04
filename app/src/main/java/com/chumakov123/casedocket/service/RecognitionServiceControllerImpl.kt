package com.chumakov123.casedocket.service

import android.content.Context
import android.content.Intent
import com.chumakov123.casedocket.domain.service.RecognitionServiceController

class RecognitionServiceControllerImpl(
    private val context: Context
) : RecognitionServiceController {

    override suspend fun ensureServiceRunning() {
        val intent = Intent(context, RecognitionForegroundService::class.java)
        context.startForegroundService(intent)
    }

    override suspend fun stopIfQueueEmpty() {
        val intent = Intent(context, RecognitionForegroundService::class.java).apply {
            action = RecognitionForegroundService.ACTION_STOP_IF_EMPTY
        }
        context.startService(intent)
    }
}