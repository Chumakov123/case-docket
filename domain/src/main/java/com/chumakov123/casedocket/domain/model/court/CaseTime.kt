package com.chumakov123.casedocket.domain.model.court

data class CaseTime(
    val hours: Int,
    val minutes: Int
)

@Suppress("DefaultLocale")
fun CaseTime.toHHMM(): String = String.format("%02d:%02d", hours, minutes)

fun String.toCaseTimeOrNull(): CaseTime? {
    val parts = split(":")
    if (parts.size != 2) return null

    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = parts[1].toIntOrNull() ?: return null

    if (hours !in 0..23 || minutes !in 0..59) return null

    return CaseTime(hours, minutes)
}