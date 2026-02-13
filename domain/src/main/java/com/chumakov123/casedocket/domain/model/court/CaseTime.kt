package com.chumakov123.casedocket.domain.model.court

data class CaseTime(
    val hours: Int,
    val minutes: Int
)

@Suppress("DefaultLocale")
fun CaseTime.toHHMM(): String = String.format("%02d:%02d", hours, minutes)