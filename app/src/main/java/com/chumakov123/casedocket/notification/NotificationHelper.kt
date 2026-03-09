package com.chumakov123.casedocket.notification

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
        val channelId = "upcoming_cases_channel"

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
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

        val notification = if (cases.size == 1) {
            val case = cases.first()
            NotificationCompat.Builder(context, channelId)
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
                .setSummaryText(context.getString(R.string.notification_summary))
            cases.forEachIndexed { index, case ->
                inboxStyle.addLine("${index + 1}. ${case.description.text}")
            }

            NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(context.getString(R.string.notification_summary))
                .setStyle(inboxStyle)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        }

        notificationManager.notify(scheduleId.toInt(), notification)
    }
}