package com.chumakov123.casedocket.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.data.mapper.toDomain
import com.chumakov123.casedocket.notification.NotificationHelper
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

        NotificationHelper.showNotification(
            context = applicationContext,
            cases = cases,
            scheduleId = scheduleId,
            scheduleDate = scheduleDate,
            judge = judge
        )
        return Result.success()
    }

    companion object {
        const val KEY_CASES = "cases"
        const val KEY_SCHEDULE_ID = "schedule_id"
        const val KEY_SCHEDULE_DATE = "schedule_date"
        const val KEY_JUDGE = "judge"
    }
}