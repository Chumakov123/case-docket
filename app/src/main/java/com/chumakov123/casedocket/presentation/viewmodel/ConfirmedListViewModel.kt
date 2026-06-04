package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.court.CaseResult
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.usecase.confirmed.GetConfirmedScheduleByIdUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.GetConfirmedSchedulesUseCase
import com.chumakov123.casedocket.domain.usecase.confirmed.UpdateConfirmedScheduleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class ConfirmedListItem {
    data class Header(val schedule: CourtSchedule) : ConfirmedListItem()
    data class Case(val case: CourtCase, val scheduleId: Long, val isPast: Boolean) :
        ConfirmedListItem()
}

class ConfirmedListViewModel(
    getConfirmedSchedulesUseCase: GetConfirmedSchedulesUseCase,
    private val getConfirmedScheduleByIdUseCase: GetConfirmedScheduleByIdUseCase,
    private val updateConfirmedScheduleUseCase: UpdateConfirmedScheduleUseCase
) : ViewModel() {

    private val now = MutableStateFlow(LocalDateTime.now())
    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived

    private val schedulesState = combine(
        getConfirmedSchedulesUseCase(now.value),
        now
    ) { (active, archived), _ ->
        Pair(active, archived)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(emptyList(), emptyList())
    )

    val activeItems: StateFlow<List<ConfirmedListItem>> = combine(
        schedulesState,
        now
    ) { (active, _), _ ->
        active.flatMap { schedule ->
            listOf(ConfirmedListItem.Header(schedule)) +
                    schedule.cases.map { case ->
                        ConfirmedListItem.Case(
                            case = case,
                            scheduleId = schedule.id,
                            isPast = case.isPast(now.value, schedule.date.value)
                        )
                    }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedItems: StateFlow<List<ConfirmedListItem>> = combine(
        schedulesState,
        now
    ) { (_, archived), _ ->
        archived.flatMap { schedule ->
            listOf(ConfirmedListItem.Header(schedule)) +
                    schedule.cases.map { case ->
                        ConfirmedListItem.Case(
                            case = case,
                            scheduleId = schedule.id,
                            isPast = true
                        )
                    }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val hasArchived: StateFlow<Boolean> = schedulesState
        .map { it.second.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun toggleShowArchived() {
        _showArchived.value = !_showArchived.value
    }

    fun refreshTime() {
        now.value = LocalDateTime.now()
    }

    fun updateCaseResult(scheduleId: Long, caseNumber: String, result: CaseResult?) {
        viewModelScope.launch {
            val schedule = getConfirmedScheduleByIdUseCase(scheduleId) ?: return@launch
            val updatedCases = schedule.cases.map {
                if (it.caseNumber == caseNumber) it.copy(result = result) else it
            }
            updateConfirmedScheduleUseCase(schedule.copy(cases = updatedCases))
        }
    }
}