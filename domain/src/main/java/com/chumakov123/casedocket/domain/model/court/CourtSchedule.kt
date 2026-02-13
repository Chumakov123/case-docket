package com.chumakov123.casedocket.domain.model.court

import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft

data class CourtSchedule(
    val date: ScheduleDate,
    val judge: Judge,
    val cases: List<CourtCaseDraft>
)