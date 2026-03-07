package com.chumakov123.casedocket.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.data.mapper.toDomain
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.toHHMM
import com.chumakov123.casedocket.presentation.activity.MainActivity
import kotlinx.serialization.json.Json

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val casesJson = inputData.getString(KEY_CASES) ?: return Result.failure()
        val scheduleId = inputData.getLong(KEY_SCHEDULE_ID, -1L)
        val scheduleDate = inputData.getString(KEY_SCHEDULE_DATE) ?: ""
        val judge = inputData.getString(KEY_JUDGE) ?: ""

        val json = Json { ignoreUnknownKeys = true }
        val cases = try {
            json.decodeFromString<List<CourtCaseDto>>(casesJson).map { it.toDomain() }
        } catch (e: Exception) {
            return Result.failure()
        }

        showNotification(cases, scheduleId, scheduleDate, judge)
        return Result.success()
    }

    private fun showNotification(
        cases: List<CourtCase>,
        scheduleId: Long,
        scheduleDate: String,
        judge: String
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "upcoming_cases_channel"

        val channel = NotificationChannel(
            channelId,
            applicationContext.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("schedule_id", scheduleId)
            putExtra("schedule_date", scheduleDate)
            putExtra("judge", judge)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val firstCase = cases.first()
        val timeString = firstCase.time.toHHMM()

        val title = if (cases.size == 1) {
            applicationContext.getString(R.string.notification_title_single, timeString)
        } else {
            applicationContext.getString(
                R.string.notification_title_multiple,
                timeString,
                cases.size
            )
        }

        val notification = if (cases.size == 1) {
            val case = cases.first()
            NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(title)
                .setContentText("${case.caseNumber} ($judge)")
                .setStyle(NotificationCompat.BigTextStyle().bigText(case.description.text))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        } else {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText(applicationContext.getString(R.string.notification_summary))
            cases.forEach { case ->
                inboxStyle.addLine("${case.time.toHHMM()} - ${case.caseNumber}")
            }

            NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(title)
                .setContentText(applicationContext.getString(R.string.notification_summary))
                .setStyle(inboxStyle)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }

        notificationManager.notify(scheduleId.toInt(), notification)
    }

    companion object {
        const val KEY_CASES = "cases"
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val KEY_SCHEDULE_DATE = "schedule_date"
        const val KEY_JUDGE = "judge"
    }
}