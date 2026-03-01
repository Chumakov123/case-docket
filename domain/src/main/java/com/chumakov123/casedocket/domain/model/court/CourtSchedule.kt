package com.chumakov123.casedocket.domain.model.court

data class CourtSchedule(
    val date: ScheduleDate,
    val judge: Judge,
    val cases: List<CourtCase>
)