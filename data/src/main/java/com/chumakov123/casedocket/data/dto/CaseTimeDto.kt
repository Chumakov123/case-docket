package com.chumakov123.casedocket.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CaseTimeDto(
    val hours: Int,
    val minutes: Int
)