package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.ErrorMessage
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtCaseDescription
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.model.court.Judge
import com.chumakov123.casedocket.domain.model.court.ScheduleDate
import com.chumakov123.casedocket.domain.model.court.draft.CourtCaseDraft
import com.chumakov123.casedocket.domain.model.court.draft.CourtScheduleDraft
import com.chumakov123.casedocket.domain.model.court.toCaseTimeOrNull
import com.chumakov123.casedocket.domain.model.court.toDraft
import com.chumakov123.casedocket.domain.model.validation.DraftValidation
import com.chumakov123.casedocket.domain.usecase.confirmed.DeleteConfirmedScheduleUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.GetConfirmedScheduleByIdUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.UpdateConfirmedScheduleUseCase
import com.chumakov123.casedocket.domain.usecase.draft.ConfirmDraftUseCase
import com.chumakov123.casedocket.domain.usecase.draft.GetDraftByIdUseCase
import com.chumakov123.casedocket.domain.usecase.draft.RejectDraftUseCase
import com.chumakov123.casedocket.domain.usecase.draft.UpdateDraftUseCase
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
    data class Error(val type: ErrorMessage) : EditDraftState() // изменено
}

sealed class EditMode {
    data class Draft(val taskId: Long) : EditMode()
    data class Confirmed(val scheduleId: Long) : EditMode()
}

class EditDraftViewModel(
    private val getDraftByIdUseCase: GetDraftByIdUseCase,
    private val updateDraftUseCase: UpdateDraftUseCase,
    private val confirmDraftUseCase: ConfirmDraftUseCase,
    private val rejectDraftUseCase: RejectDraftUseCase,
    private val getConfirmedScheduleUseCase: GetConfirmedScheduleByIdUseCase,
    private val updateConfirmedScheduleUseCase: UpdateConfirmedScheduleUseCase,
    private val deleteConfirmedScheduleUseCase: DeleteConfirmedScheduleUseCase,
    private val scheduleValidator: ScheduleValidator,
) : ViewModel() {

    private val _mode = MutableStateFlow<EditMode?>(null)
    val mode: StateFlow<EditMode?> = _mode

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

    fun setMode(mode: EditMode) {
        _mode.value = mode
        when (mode) {
            is EditMode.Draft -> loadDraft(mode.taskId)
            is EditMode.Confirmed -> loadConfirmed(mode.scheduleId)
        }
    }

    private fun loadDraft(taskId: Long) {
        viewModelScope.launch {
            _state.value = EditDraftState.Loading
            try {
                val draft = getDraftByIdUseCase(taskId)
                if (draft != null) {
                    _currentDraft.value = draft
                    _state.value = EditDraftState.Success
                } else {
                    _state.value = EditDraftState.Error(ErrorMessage.DraftNotFound)
                }
            } catch (e: Exception) {
                _state.value = EditDraftState.Error(
                    ErrorMessage.LoadingError(e.message ?: "")
                )
            }
        }
    }

    private fun loadConfirmed(scheduleId: Long) {
        viewModelScope.launch {
            _state.value = EditDraftState.Loading
            try {
                val schedule = getConfirmedScheduleUseCase(scheduleId)
                if (schedule != null) {
                    _currentDraft.value = schedule.toDraft()
                    _state.value = EditDraftState.Success
                } else {
                    _state.value = EditDraftState.Error(ErrorMessage.ScheduleNotFound)
                }
            } catch (e: Exception) {
                _state.value = EditDraftState.Error(
                    ErrorMessage.LoadingError(e.message ?: "")
                )
            }
        }
    }

    private fun scheduleAutoSave(draft: CourtScheduleDraft) {
        if (_mode.value !is EditMode.Draft) return
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(500)
            val id = (_mode.value as? EditMode.Draft)?.taskId ?: return@launch
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

    fun updateCasePreliminary(index: Int, isPreliminary: Boolean) {
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(isPreliminary = isPreliminary)
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun updateCaseVideoConference(index: Int, isVideoConference: Boolean) {
        _currentDraft.update { draft ->
            draft?.let {
                val updatedCases = it.cases.toMutableList().apply {
                    this[index] = this[index].copy(isVideoConference = isVideoConference)
                }
                it.copy(cases = updatedCases)
            }
        }
    }

    fun confirmDraft(onComplete: () -> Unit) {
        val draft = _currentDraft.value ?: return
        val mode = _mode.value as? EditMode.Draft ?: return
        if (!_validation.value.isValid) return

        viewModelScope.launch {
            try {
                val confirmedSchedule = draft.toCourtSchedule().copy(id = 0)
                confirmDraftUseCase(mode.taskId, confirmedSchedule)
                onComplete()
            } catch (e: Exception) {
                _state.value = EditDraftState.Error(
                    ErrorMessage.ConfirmationError(e.message ?: "")
                )
            }
        }
    }

    fun rejectDraft(onComplete: () -> Unit) {
        val mode = _mode.value as? EditMode.Draft ?: return
        viewModelScope.launch {
            try {
                rejectDraftUseCase(mode.taskId)
                onComplete()
            } catch (e: Exception) {
                _state.value = EditDraftState.Error(
                    ErrorMessage.RejectionError(e.message ?: "")
                )
            }
        }
    }

    fun saveConfirmed(onComplete: () -> Unit) {
        val draft = _currentDraft.value ?: return
        val mode = _mode.value as? EditMode.Confirmed ?: return
        if (!_validation.value.isValid) return

        viewModelScope.launch {
            try {
                val schedule = draft.toCourtSchedule().copy(id = mode.scheduleId)
                updateConfirmedScheduleUseCase(schedule)
                onComplete()
            } catch (e: Exception) {
                _state.value = EditDraftState.Error(
                    ErrorMessage.SaveError(e.message ?: "")
                )
            }
        }
    }

    fun deleteConfirmed(onComplete: () -> Unit) {
        val mode = _mode.value as? EditMode.Confirmed ?: return
        viewModelScope.launch {
            try {
                deleteConfirmedScheduleUseCase(mode.scheduleId)
                onComplete()
            } catch (e: Exception) {
                _state.value = EditDraftState.Error(
                    ErrorMessage.DeletionError(e.message ?: "")
                )
            }
        }
    }

    fun addCase() {
        _currentDraft.update { draft ->
            draft?.let {
                val newCase = CourtCaseDraft(
                    caseNumber = null,
                    time = null,
                    description = CourtCaseDescription("")
                )
                it.copy(cases = it.cases + newCase)
            }
        }
    }

    fun deleteCase(index: Int) {
        _currentDraft.update { draft ->
            draft?.let {
                if (index in it.cases.indices) {
                    val updatedCases = it.cases.toMutableList().apply { removeAt(index) }
                    it.copy(cases = updatedCases)
                } else {
                    it
                }
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
    description = description,
    isPreliminary = isPreliminary,
    isVideoConference = isVideoConference
)