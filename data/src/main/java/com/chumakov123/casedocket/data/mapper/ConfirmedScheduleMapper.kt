package com.chumakov123.casedocket.data.mapper

import com.chumakov123.casedocket.data.dto.CourtCaseDto
import com.chumakov123.casedocket.data.local.entity.ConfirmedScheduleEntity
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import kotlinx.serialization.json.Json

fun CourtSchedule.toConfirmedScheduleEntity(json: Json, id: Long = 0): ConfirmedScheduleEntity {
    val casesDto = this.cases.map { it.toDto() }
    val casesJson = json.encodeToString<List<CourtCaseDto>>(casesDto)
    return ConfirmedScheduleEntity(
        id = id,
        date = this.date.toDisplayFormat(),
        judge = this.judge.text,
        casesJson = casesJson,
        createdAt = System.currentTimeMillis()
    )
}

fun ConfirmedScheduleEntity.toCourtSchedule(json: Json): CourtSchedule? {
    val casesDto = try {
        json.decodeFromString<List<CourtCaseDto>>(this.casesJson)
    } catch (e: Exception) {
        return null
    }
    val cases = casesDto.map { it.toDomain() }
    val date = ScheduleDate.parse(this.date) ?: return null
    return CourtSchedule(
        date = date,
        judge = Judge(this.judge),
        cases = cases
    )
}