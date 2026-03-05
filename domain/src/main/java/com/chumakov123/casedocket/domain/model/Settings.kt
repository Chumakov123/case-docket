package com.chumakov123.casedocket.domain.model

data class Settings(
    val language: String, // "ru" или "en"
    val theme: String,    // "system", "light", "dark"
    val notificationMinutes: Int // 0 = отключено
)
