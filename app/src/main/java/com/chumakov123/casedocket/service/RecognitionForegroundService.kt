package com.chumakov123.casedocket.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.data.dto.CourtScheduleDraftDto
import com.chumakov123.casedocket.data.mapper.toDto
import com.chumakov123.casedocket.domain.model.RecognitionTask
import com.chumakov123.casedocket.domain.service.ScheduleRecognitionManager
import com.chumakov123.casedocket.domain.usecase.RecognizeScheduleUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import kotlin.coroutines.cancellation.CancellationException

class RecognitionForegroundService : LifecycleService() {

    private val manager: ScheduleRecognitionManager by inject()
    private val recognizeUseCase: RecognizeScheduleUseCase by inject()
    private var processingJob: Job? = null

    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Сервис запущен"))

        lifecycleScope.launch {
            repairStuckTasks()
            processQueue()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP_IF_EMPTY -> lifecycleScope.launch { stopIfQueueEmpty() }
        }
        return START_STICKY
    }

    private suspend fun repairStuckTasks() {
        manager.repairStuckTasks()
    }

    private fun processQueue() {
        processingJob = lifecycleScope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val task = manager.processNextTask() ?: break
                    processTask(task)
                }
            } catch (e: CancellationException) {

            } finally {
                stopSelf()
            }
        }
    }

    private suspend fun processTask(task: RecognitionTask) {
        updateNotification("Обработка задачи ${task.id}")
        try {
            val imageBytes = loadImageBytes(task.imageUri)
            val result = withTimeout(120_000) {
                recognizeUseCase.execute(imageBytes)
            }
            val dto = result.toDto()
            val json = Json.Default.encodeToString(CourtScheduleDraftDto.serializer(), dto)
            manager.completeTask(task.id, json)
        } catch (e: TimeoutCancellationException) {
            manager.failTask(task.id, "OCR timeout")
        } catch (e: Exception) {
            manager.failTask(task.id, e.message ?: "Unknown error")
        }
    }

    private suspend fun stopIfQueueEmpty() {
        if (!manager.hasPendingTask()) {
            processingJob?.cancel()
            stopSelf()
        }
    }

    private suspend fun loadImageBytes(uri: String): ByteArray {
        return withContext(Dispatchers.IO) {
            contentResolver.openInputStream(uri.toUri())?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Cannot open image: $uri")
        }
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Распознавание расписания")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Распознавание",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Уведомления о процессе распознавания"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "recognition_channel"
        const val ACTION_STOP_IF_EMPTY = "stop_if_empty"
    }
}