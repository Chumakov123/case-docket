package com.chumakov123.casedocket.data.mapper

import com.chumakov123.casedocket.data.dto.CaseResultDto
import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.domain.model.court.CaseResult
import com.chumakov123.casedocket.domain.model.court.CourtCase

fun CourtCase.toDto(): CourtCaseDto = CourtCaseDto(
    caseNumber = this.caseNumber,
    time = this.time.toDto(),
    description = this.description.toDto(),
    isPreliminary = this.isPreliminary,
    isVideoConference = this.isVideoConference,
    result = this.result?.toDto()
)

fun CourtCaseDto.toDomain(): CourtCase = CourtCase(
    caseNumber = this.caseNumber,
    time = this.time.toDomain(),
    description = this.description.toDomain(),
    isPreliminary = this.isPreliminary,
    isVideoConference = this.isVideoConference,
    result = this.result?.toDomain()
)

fun CaseResult.toDto(): CaseResultDto = when (this) {
    CaseResult.RECESS -> CaseResultDto.RECESS
    CaseResult.ADJOURNMENT -> CaseResultDto.ADJOURNMENT
    CaseResult.EXPERTISE -> CaseResultDto.EXPERTISE
    CaseResult.RESTARTED -> CaseResultDto.RESTARTED
    CaseResult.DECISION -> CaseResultDto.DECISION
}

fun CaseResultDto.toDomain(): CaseResult = when (this) {
    CaseResultDto.RECESS -> CaseResult.RECESS
    CaseResultDto.ADJOURNMENT -> CaseResult.ADJOURNMENT
    CaseResultDto.EXPERTISE -> CaseResult.EXPERTISE
    CaseResultDto.RESTARTED -> CaseResult.RESTARTED
    CaseResultDto.DECISION -> CaseResult.DECISION
}