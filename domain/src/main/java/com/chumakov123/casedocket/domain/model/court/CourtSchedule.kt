package com.chumakov123.casedocket.domain.model.court

import java.time.LocalDateTime

data class CourtSchedule(
    val id: Long = 0,
    val date: ScheduleDate,
    val judge: Judge,
    val cases: List<CourtCase>
) {
    fun hasFutureCases(now: LocalDateTime): Boolean {
        return cases.any { !it.isPast(now, date.value) }
    }

    fun getEarliestFutureCaseTime(now: LocalDateTime): LocalDateTime? {
        return cases.mapNotNull { case ->
            if (!case.isPast(now, date.value)) {
                LocalDateTime.of(
                    date.value,
                    java.time.LocalTime.of(case.time.hours, case.time.minutes)
                )
            } else null
        }.minOrNull()
    }
}