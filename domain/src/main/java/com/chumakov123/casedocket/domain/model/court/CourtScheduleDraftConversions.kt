package com.chumakov123.casedocket.domain.model.court

import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft

fun CourtSchedule.toDraft(): CourtScheduleDraft = CourtScheduleDraft(
    date = this.date,
    judge = this.judge,
    cases = this.cases.map { it.toDraft() }
)

fun CourtCase.toDraft(): CourtCaseDraft = CourtCaseDraft(
    caseNumber = this.caseNumber,
    time = this.time,
    description = this.description
)