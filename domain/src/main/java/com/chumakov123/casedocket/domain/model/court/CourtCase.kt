package com.chumakov123.casedocket.domain.model.court

import java.time.LocalDate
import java.time.LocalDateTime

data class CourtCase(
    val caseNumber: String,
    val time: CaseTime,
    val description: CourtCaseDescription
) {
    fun isPast(now: LocalDateTime, date: LocalDate): Boolean {
        val caseDateTime = LocalDateTime.of(date, java.time.LocalTime.of(time.hours, time.minutes))
        return caseDateTime.isBefore(now)
    }
}