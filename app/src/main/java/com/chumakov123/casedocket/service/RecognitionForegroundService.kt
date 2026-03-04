package com.chumakov123.casedocket.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import com.chumakov123.casedocket.presentation.activity.MainActivity
import com.chumakov123.casedocket.presentation.tracker.AppForegroundTracker
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

    private val foregroundTracker: AppForegroundTracker by inject()
    private val manager: ScheduleRecognitionManager by inject()
    private val recognizeUseCase: RecognizeScheduleUseCase by inject()
    private var processingJob: Job? = null
    private var processedTasksCount = 0

    private val notificationManager by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createCompletionNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Сервис запущен"))

        lifecycleScope.launch {
            repairStuckTasks()
            processQueue()
        }
    }

    private fun createCompletionNotificationChannel() {
        val channel = NotificationChannel(
            COMPLETION_CHANNEL_ID,
            "Завершение распознавания",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о завершении обработки всех изображений"
        }
        notificationManager.createNotificationChannel(channel)
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
                if (processedTasksCount > 0) {
                    showCompletionNotification()
                }
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
            processedTasksCount++
        } catch (e: TimeoutCancellationException) {
            manager.failTask(task.id, "OCR timeout")
            processedTasksCount++
        } catch (e: Exception) {
            manager.failTask(task.id, e.message ?: "Unknown error")
            processedTasksCount++
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

    private fun showCompletionNotification() {
        if (foregroundTracker.isAppInForeground) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, COMPLETION_CHANNEL_ID)
            .setContentTitle("Распознавание завершено")
            .setContentText("Все изображения обработаны")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "recognition_channel"
        const val ACTION_STOP_IF_EMPTY = "stop_if_empty"

        const val COMPLETION_NOTIFICATION_ID = 1002
        private const val COMPLETION_CHANNEL_ID = "recognition_completion_channel"
    }
}