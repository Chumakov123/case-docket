package com.chumakov123.casedocket.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chumakov123.casedocket.data.alarm.NotifiedAlarmStore
import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.data.mapper.toDomain
import com.chumakov123.casedocket.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val notifiedAlarmStore: NotifiedAlarmStore by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val casesJson = intent.getStringExtra(EXTRA_CASES) ?: return
        val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
        val scheduleDate = intent.getStringExtra(EXTRA_SCHEDULE_DATE) ?: ""
        val judge = intent.getStringExtra(EXTRA_JUDGE) ?: ""
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        val json = Json { ignoreUnknownKeys = true }
        val cases = try {
            json.decodeFromString<List<CourtCaseDto>>(casesJson).map { it.toDomain() }
        } catch (_: Exception) {
            return
        }

        if (requestCode != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                notifiedAlarmStore.markAsNotified(requestCode)
            }
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
        const val EXTRA_REQUEST_CODE = "request_code"
    }
}
