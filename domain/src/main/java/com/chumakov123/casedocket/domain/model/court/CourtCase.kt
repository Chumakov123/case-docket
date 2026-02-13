package com.chumakov123.casedocket.domain.model.court

data class CourtCase(
    val caseNumber: String,
    val time: CaseTime,
    val description: CourtCaseDescription
)
