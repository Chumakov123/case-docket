package com.chumakov123.casedocket.domain.model.court.draft

import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate

data class CourtScheduleDraft(
    val date: ScheduleDate?,
    val judge: Judge,
    val cases: List<CourtCaseDraft>
)