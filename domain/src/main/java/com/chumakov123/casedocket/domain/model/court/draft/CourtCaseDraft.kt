package com.chumakov123.casedocket.domain.model.court.draft

import com.chumakov123.casedocket.domain.model.court.CaseTime
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription

data class CourtCaseDraft(
    val caseNumber: String?,
    val time: CaseTime?,
    val description: CourtCaseDescription,
    val isPreliminary: Boolean = false,
    val isVideoConference: Boolean = false,
)