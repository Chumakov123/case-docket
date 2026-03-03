package com.chumakov123.casedocket.data.mapper

import com.chumakov123.casedocket.data.dto.CaseTimeDto
import com.chumakov123.casedocket.data.dto.CourtCaseDescriptionDto
import com.chumakov123.casedocket.data.dto.CourtCaseDraftDto
import com.chumakov123.casedocket.data.dto.CourtScheduleDraftDto
import com.chumakov123.casedocket.data.dto.JudgeDto
import com.chumakov123.casedocket.data.dto.ScheduleDateDto
import com.chumakov123.casedocket.domain.model.court.CaseTime
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft

// ---------- domain -> DTO ----------

fun ScheduleDate.toDto(): ScheduleDateDto = ScheduleDateDto(value)

fun CaseTime.toDto(): CaseTimeDto = CaseTimeDto(hours, minutes)

fun CourtCaseDescription.toDto(): CourtCaseDescriptionDto = CourtCaseDescriptionDto(text)

fun Judge.toDto(): JudgeDto = JudgeDto(text)

fun CourtCaseDraft.toDto(): CourtCaseDraftDto = CourtCaseDraftDto(
    caseNumber = caseNumber,
    time = time?.toDto(),
    description = description.toDto()
)

fun CourtScheduleDraft.toDto(): CourtScheduleDraftDto = CourtScheduleDraftDto(
    date = date?.toDto(),
    judge = judge.toDto(),
    cases = cases.map { it.toDto() }
)

// ---------- DTO -> domain ----------

fun ScheduleDateDto.toDomain(): ScheduleDate = ScheduleDate(value)

fun CaseTimeDto.toDomain(): CaseTime = CaseTime(hours, minutes)

fun CourtCaseDescriptionDto.toDomain(): CourtCaseDescription = CourtCaseDescription(text)

fun JudgeDto.toDomain(): Judge = Judge(text)

fun CourtCaseDraftDto.toDomain(): CourtCaseDraft = CourtCaseDraft(
    caseNumber = caseNumber,
    time = time?.toDomain(),
    description = description.toDomain()
)

fun CourtScheduleDraftDto.toDomain(): CourtScheduleDraft = CourtScheduleDraft(
    date = date?.toDomain(),
    judge = judge.toDomain(),
    cases = cases.map { it.toDomain() }
)