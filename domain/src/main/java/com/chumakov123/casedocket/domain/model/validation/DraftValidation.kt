package com.chumakov123.casedocket.domain.model.validation

data class DraftValidation(
    val isValid: Boolean,
    val dateError: Boolean,
    val judgeError: Boolean,
    val casesError: Boolean,
    val casesValidations: List<CaseValidation>
)
