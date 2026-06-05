package com.chumakov123.casedocket.worker

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.chumakov123.casedocket.data.mapper.toDto
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.NotificationScheduler
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

class WorkManagerNotificationScheduler(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val json: Json
) : NotificationScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleAllNotifications(schedules: List<CourtSchedule>) {
        Timber.d("Scheduling all notifications. Total schedules: ${schedules.size}")
        cancelAllNotifications()

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
                if (caseDateTime.isAfter(now)) {
                    Triple(schedule, case, caseDateTime)
                } else null
            }
        }

        val groupedByNotifyTime = futureCases.groupBy { (_, _, dateTime) ->
            dateTime.minusMinutes(notificationMinutes.toLong())
        }

        groupedByNotifyTime.forEach { (notifyTime, triples) ->
            val cases = triples.map { it.second }
            val firstSchedule = triples.first().first

            val inputData = workDataOf(
                NotificationWorker.KEY_CASES to json.encodeToString(cases.map { it.toDto() }),
                NotificationWorker.KEY_SCHEDULE_ID to firstSchedule.id,
                NotificationWorker.KEY_SCHEDULE_DATE to firstSchedule.date.toDisplayFormat(),
                NotificationWorker.KEY_JUDGE to firstSchedule.judge.text
            )

            val delay = Duration.between(now, notifyTime).toMillis()
            if (delay < 0) {
                Timber.d("Notification for schedule ${firstSchedule.id} with time $notifyTime is in the past. Skipping.")
                return@forEach
            }

            val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("notification_${firstSchedule.id}_${notifyTime.toEpochSecond(ZoneOffset.UTC)}")
                .build()

            workManager.enqueue(request)
            Timber.d("Scheduled notification for schedule ID: ${firstSchedule.id}, notify time: $notifyTime, delay: $delay ms, cases: ${cases.map { it.caseNumber }}")
        }
    }

    override suspend fun cancelAllNotifications() {
        Timber.d("Cancelling all existing notifications.")
        workManager.cancelAllWork()
    }
}