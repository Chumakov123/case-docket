package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.court.CourtCase
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class ConfirmedListItem {
    data class Header(val schedule: CourtSchedule) : ConfirmedListItem()
    data class Case(val case: CourtCase, val scheduleId: Long) : ConfirmedListItem()
}

class ConfirmedListViewModel(
    confirmedRepository: ConfirmedScheduleRepository
) : ViewModel() {

    val items: StateFlow<List<ConfirmedListItem>> = confirmedRepository.observeAllSchedules()
        .map { schedules ->
            schedules.sortedByDescending { it.date.value }
                .flatMap { schedule ->
                    listOf(ConfirmedListItem.Header(schedule)) +
                            schedule.cases.map { case -> ConfirmedListItem.Case(case, schedule.id) }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}