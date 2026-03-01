package com.chumakov123.casedocket.domain.validator

import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.model.validation.CaseValidation
import com.chumakov123.casedocket.domain.model.validation.DraftValidation

class ScheduleValidator {
    fun validate(draft: CourtScheduleDraft): DraftValidation {
        val dateError = draft.date == null
        val judgeError = draft.judge.text.isBlank()
        val casesError = draft.cases.isEmpty()

        val casesValidations = draft.cases.map { case ->
            CaseValidation(
                caseNumberError = case.caseNumber.isNullOrBlank(),
                timeError = case.time == null,
                descriptionError = case.description.text.isBlank()
            )
        }

        val isValid = !dateError && !judgeError && !casesError &&
                casesValidations.all { !it.caseNumberError && !it.timeError && !it.descriptionError }

        return DraftValidation(
            isValid = isValid,
            dateError = dateError,
            judgeError = judgeError,
            casesError = casesError,
            casesValidations = casesValidations
        )
    }
}