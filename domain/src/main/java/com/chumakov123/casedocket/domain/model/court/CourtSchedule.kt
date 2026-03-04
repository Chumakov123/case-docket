package com.chumakov123.casedocket.domain.model.court

data class CourtSchedule(
    val id: Long = 0,
    val date: ScheduleDate,
    val judge: Judge,
    val cases: List<CourtCase>
)