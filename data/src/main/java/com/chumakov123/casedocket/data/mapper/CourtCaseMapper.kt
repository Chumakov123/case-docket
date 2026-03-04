package com.chumakov123.casedocket.data.mapper

import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.domain.model.court.CourtCase

fun CourtCase.toDto(): CourtCaseDto = CourtCaseDto(
    caseNumber = this.caseNumber,
    time = this.time.toDto(),
    description = this.description.toDto()
)

fun CourtCaseDto.toDomain(): CourtCase = CourtCase(
    caseNumber = this.caseNumber,
    time = this.time.toDomain(),
    description = this.description.toDomain()
)