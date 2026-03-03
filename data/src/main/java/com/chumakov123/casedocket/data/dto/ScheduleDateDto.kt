package com.chumakov123.casedocket.data.dto

import com.chumakov123.casedocket.data.serializer.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
@JvmInline
value class ScheduleDateDto(
    @Serializable(with = LocalDateSerializer::class)
    val value: LocalDate
)