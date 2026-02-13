package com.chumakov123.casedocket.domain.model.court

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JvmInline
value class ScheduleDate(val value: LocalDate) {
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        fun parse(text: String): ScheduleDate? = runCatching {
            ScheduleDate(LocalDate.parse(text, formatter))
        }.getOrNull()
    }

    fun format(pattern: String = "dd.MM.yyyy"): String =
        value.format(DateTimeFormatter.ofPattern(pattern))

    fun toDisplayFormat(): String = format("dd.MM.yyyy")
}