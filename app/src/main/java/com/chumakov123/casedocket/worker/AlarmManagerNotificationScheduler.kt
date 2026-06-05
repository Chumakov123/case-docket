package com.chumakov123.casedocket.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.chumakov123.casedocket.data.alarm.ActiveAlarmStore
import com.chumakov123.casedocket.data.alarm.NotifiedAlarmStore
import com.chumakov123.casedocket.data.mapper.toDto
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.NotificationScheduler
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import com.chumakov123.casedocket.receiver.AlarmReceiver
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmManagerNotificationScheduler(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val json: Json,
    private val workManagerScheduler: NotificationScheduler,
    private val activeAlarmStore: ActiveAlarmStore,
    private val notifiedAlarmStore: NotifiedAlarmStore
) : NotificationScheduler {

    override suspend fun scheduleAllNotifications(schedules: List<CourtSchedule>) {
        Timber.d("Scheduling all notifications. Total schedules: ${schedules.size}")
        cancelAllNotifications()
        notifiedAlarmStore.clearOld(24 * 60 * 60 * 1000L)

        if (!hasExactAlarmPermission()) {
            Timber.d("No exact alarm permission, falling back to WorkManager")
            workManagerScheduler.scheduleAllNotifications(schedules)
            return
        }

        val settings = settingsRepository.observeSettings().first()
        val notificationMinutes = settings.notificationMinutes
        if (notificationMinutes == 0) {
            Timber.d("Notifications disabled (notificationMinutes is 0). No notifications will be scheduled.")
            return
        }

        val now = LocalDateTime.now()

        val futureCases = schedules.flatMap { schedule ->
            schedule.cases.mapNotNull { case ->
                // Фильтр по ПСЗ
                if (!settings.notifyPreliminary && case.isPreliminary) {
                    Timber.d("Filtering out preliminary case: ${case.caseNumber}")
                    return@mapNotNull null
                }

                val caseDateTime = LocalDateTime.of(
                    schedule.date.value,
                    LocalTime.of(case.time.hours, case.time.minutes)
                )
                // Оставляем только те дела, время которых еще не наступило
                if (caseDateTime.isAfter(now)) {
                    Triple(schedule, case, caseDateTime)
                } else null
            }
        }

        val groupedByNotifyTime = futureCases.groupBy { (_, _, dateTime) ->
            dateTime.minusMinutes(notificationMinutes.toLong())
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        groupedByNotifyTime.forEach { (notifyTime, triples) ->
            val cases = triples.map { it.second }
            val firstSchedule = triples.first().first

            val casesJson = json.encodeToString(cases.map { it.toDto() })

            val requestCode = (firstSchedule.id.hashCode() + notifyTime.toEpochSecond(
                ZoneId.systemDefault().rules.getOffset(notifyTime)
            ).hashCode()).hashCode()

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_CASES, casesJson)
                putExtra(AlarmReceiver.EXTRA_SCHEDULE_ID, firstSchedule.id)
                putExtra(AlarmReceiver.EXTRA_SCHEDULE_DATE, firstSchedule.date.toDisplayFormat())
                putExtra(AlarmReceiver.EXTRA_JUDGE, firstSchedule.judge.text)
                putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = if (notifyTime.isAfter(now)) {
                notifyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                // Если время уведомления (за X минут) уже прошло, но дело еще не началось,
                // проверяем, не показывали ли мы его уже
                if (notifiedAlarmStore.isNotified(requestCode)) {
                    Timber.d("Notification for requestCode $requestCode already shown. Skipping.")
                    return@forEach
                }
                // Если нет — ставим будильник на "сейчас + 1 секунда"
                System.currentTimeMillis() + 1000
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )

            activeAlarmStore.addRequestCode(requestCode)
            Timber.d(
                "Scheduled alarm for schedule ID: ${firstSchedule.id}, notify time: $notifyTime, requestCode: $requestCode, delay: ${triggerTime - System.currentTimeMillis()} ms, cases: ${
                    cases.map { it.caseNumber }.toList()
                }"
            )
        }
    }

    override suspend fun cancelAllNotifications() {
        Timber.d("Cancelling all existing alarms.")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCodes = activeAlarmStore.getAllRequestCodes()

        requestCodes.forEach { requestCode ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Timber.d("Cancelled alarm with requestCode: $requestCode")
            }
        }

        activeAlarmStore.clearAll()
        workManagerScheduler.cancelAllNotifications()
        Timber.d("Cleared all active alarm request codes and cancelled WorkManager notifications.")
    }

    private fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}