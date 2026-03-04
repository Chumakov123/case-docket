package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.model.court.toCaseTimeOrNull
import com.chumakov123.casedocket.domain.model.validation.DraftValidation
import com.chumakov123.casedocket.domain.usecase.ConfirmDraftUseCase
import com.chumakov123.casedocket.domain.usecase.GetDraftByIdUseCase
import com.chumakov123.casedocket.domain.usecase.RejectDraftUseCase
import com.chumakov123.casedocket.domain.usecase.UpdateDraftUseCase
import com.chumakov123.casedocket.domain.validator.ScheduleValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class EditDraftState {
    object Idle : EditDraftState()
    object Loading : EditDraftState()
    object Success : EditDraftState()
    data class Error(val message: String) : EditDraftState()
}

class EditDraftViewModel(
    private val getDraftByIdUseCase: GetDraftByIdUseCase,
    private val updateDraftUseCase: UpdateDraftUseCase,
    private val confirmDraftUseCase: ConfirmDraftUseCase,
    private val rejectDraftUseCase: RejectDraftUseCase,
    private val scheduleValidator: ScheduleValidator,
) : ViewModel() {

    private val _taskId = MutableStateFlow<Long?>(null)

    private val _currentDraft = MutableStateFlow<CourtScheduleDraft?>(null)
    val currentDraft: StateFlow<CourtScheduleDraft?> = _currentDraft

    private val _validation = MutableStateFlow(
        DraftValidation(
            isValid = false,
            dateError = false,
            judgeError = false,
            casesError = false,
            casesValidations = emptyList()
        )
    )
    val validation: StateFlow<DraftValidation> = _validation

    private val _state = MutableStateFlow<EditDraftState>(EditDraftState.Idle)
    val state: StateFlow<EditDraftState> = _state

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            _currentDraft.collectLatest { draft ->
                if (draft != null) {
                    _validation.value = scheduleValidator.validate(draft)
                    scheduleAutoSave(draft)
                }
            }
        }
    }

    fun setTaskId(id: Long) {
        _taskId.value = id
        loadDraft(id)
    }

    private fun loadDraft(id: Long) {
        viewModelScope.launch {
            _state.value = EditDraftState.Loading
            try {
                val draft = getDraftByIdUseCase(id)
                if (draft != null) {
                    _currentDraft.value = draft
                    _state.value = EditDraftState.Success
                } else {
                    _state.value = EditDraftState.Error("Черновик не найден")
                }
            } catch (e: Exception) {
                _state.value = EditDraftState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun scheduleAutoSave(draft: CourtScheduleDraft) {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500)
            val id = _taskId.value ?: return@launch
            try {
                updateDraftUseCase(id, draft)
            } catch (e: Exception) {

            }
        }
    }

    fun updateJudge(text: String) {
        _currentDraft.update { draft ->
            draft?.copy(judge = Judge(text))
        }
    }

    fun updateDate(date: ScheduleDate) {
        _currentDraft.update { draft ->
            draft?.copy(date = date)
        }
    }

    fun updateCaseNumber(index: Int, number: String) {
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(caseNumber = number.ifBlank { null })
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun updateCaseTime(index: Int, timeString: String) {
        val caseTime = timeString.toCaseTimeOrNull()
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(time = caseTime)
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun updateCaseDescription(index: Int, description: String) {
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(description = CourtCaseDescription(description))
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun confirmDraft(onComplete: () -> Unit) {
        val draft = _currentDraft.value ?: return
        val id = _taskId.value ?: return
        if (!_validation.value.isValid) return

        viewModelScope.launch {
            try {
                val confirmedSchedule = draft.toCourtSchedule()
                confirmDraftUseCase(id, confirmedSchedule)
                onComplete()
            } catch (e: Exception) {
                _state.value = EditDraftState.Error("Ошибка подтверждения: ${e.message}")
            }
        }
    }

    fun rejectDraft(onComplete: () -> Unit) {
        val id = _taskId.value ?: return
        viewModelScope.launch {
            try {
                rejectDraftUseCase(id)
                onComplete()
            } catch (e: Exception) {
                _state.value = EditDraftState.Error("Ошибка отклонения: ${e.message}")
            }
        }
    }
}

fun CourtScheduleDraft.toCourtSchedule(): CourtSchedule = CourtSchedule(
    date = requireNotNull(date) { "Date must not be null" },
    judge = judge,
    cases = cases.map { it.toCourtCase() }
)

fun CourtCaseDraft.toCourtCase(): CourtCase = CourtCase(
    caseNumber = requireNotNull(caseNumber) { "Case number must not be null" },
    time = requireNotNull(time) { "Time must not be null" },
    description = description
)