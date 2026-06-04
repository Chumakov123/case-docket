package com.chumakov123.casedocket.domain.model.court

enum class CaseResult {
    RECESS,                 // Перерыв
    ADJOURNMENT,            // Отложение
    EXPERTISE,              // Экспертиза
    RESTARTED,              // Рассмотрение начато с начала
    DECISION                // Вынесено решение
}
