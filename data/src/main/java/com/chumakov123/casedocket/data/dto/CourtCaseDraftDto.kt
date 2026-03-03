package com.chumakov123.casedocket.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourtCaseDraftDto(
    val caseNumber: String?,
    val time: CaseTimeDto?,
    val description: CourtCaseDescriptionDto,
)


