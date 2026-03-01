package com.chumakov123.casedocket.domain.model.validation

data class CaseValidation(
    val caseNumberError: Boolean,
    val timeError: Boolean,
    val descriptionError: Boolean
)
