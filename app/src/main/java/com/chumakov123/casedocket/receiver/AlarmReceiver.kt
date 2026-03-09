package com.chumakov123.casedocket.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.data.mapper.toDomain
import com.chumakov123.casedocket.notification.NotificationHelper
import kotlinx.serialization.json.Json

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val casesJson = intent.getStringExtra(EXTRA_CASES) ?: return
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val scheduleDate = intent.getStringExtra(EXTRA_SCHEDULE_DATE) ?: ""
        val judge = intent.getStringExtra(EXTRA_JUDGE) ?: ""

        val json = Json { ignoreUnknownKeys = true }
        val cases = try {
            json.decodeFromString<List<CourtCaseDto>>(casesJson).map { it.toDomain() }
        } catch (e: Exception) {
            return
        }

        NotificationHelper.showNotification(
            context = context,
            cases = cases,
            scheduleId = scheduleId,
            scheduleDate = scheduleDate,
            judge = judge
        )
    }

    companion object {
        const val EXTRA_CASES = "cases"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val EXTRA_SCHEDULE_DATE = "schedule_date"
        const val EXTRA_JUDGE = "judge"
    }
}