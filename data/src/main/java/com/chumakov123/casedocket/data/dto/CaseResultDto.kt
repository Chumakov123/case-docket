package com.chumakov123.casedocket.data.dto

import kotlinx.serialization.Serializable

@Serializable
enum class CaseResultDto {
    RECESS,
    ADJOURNMENT,
    EXPERTISE,
    RESTARTED,
    DECISION
}
