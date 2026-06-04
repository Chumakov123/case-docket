package com.chumakov123.casedocket.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.chumakov123.casedocket.R
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.toHHMM
import com.chumakov123.casedocket.presentation.activity.MainActivity

object NotificationHelper {

    fun showNotification(
        context: Context,
        cases: List<CourtCase>,
        scheduleId: Long,
        scheduleDate: String,
        judge: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Используем новый ID канала, чтобы форсировать HIGH IMPORTANCE на устройствах, 
        // где канал уже был создан с DEFAULT
        val channelId = "upcoming_cases_channel_v2"

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("schedule_id", scheduleId)
            putExtra("schedule_date", scheduleDate)
            putExtra("judge", judge)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val firstCase = cases.first()
        val timeString = firstCase.time.toHHMM()

        val title = if (cases.size == 1) {
            context.getString(R.string.notification_title_single, timeString)
        } else {
            context.getString(R.string.notification_title_multiple, timeString, cases.size)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notification = if (cases.size == 1) {
            val case = cases.first()
            val pszPrefix = if (case.isPreliminary) "${context.getString(R.string.psz)} " else ""
            val vksPrefix =
                if (case.isVideoConference) "${context.getString(R.string.vks)} " else ""
            builder.setContentTitle(title)
                .setContentText("$pszPrefix$vksPrefix${case.caseNumber} ($judge)")
                .setStyle(NotificationCompat.BigTextStyle().bigText(case.description.text))
                .build()
        } else {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .setSummaryText(context.getString(R.string.notification_summary))
            cases.forEachIndexed { index, case ->
                val pszPrefix =
                    if (case.isPreliminary) "${context.getString(R.string.psz)} " else ""
                val vksPrefix =
                    if (case.isVideoConference) "${context.getString(R.string.vks)} " else ""
                inboxStyle.addLine("${index + 1}. $pszPrefix$vksPrefix${case.description.text}")
            }

            builder.setContentTitle(title)
                .setContentText(context.getString(R.string.notification_summary))
                .setStyle(inboxStyle)
                .build()
        }

        notificationManager.notify(scheduleId.toInt(), notification)
    }
}