package com.chumakov123.casedocket.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourtScheduleDraftDto(
    val date: ScheduleDateDto?,
    val judge: JudgeDto,
    val cases: List<CourtCaseDraftDto>
)