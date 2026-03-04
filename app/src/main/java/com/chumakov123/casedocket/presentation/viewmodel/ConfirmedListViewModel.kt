package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.court.CourtSchedule
import com.chumakov123.casedocket.domain.repository.ConfirmedScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ConfirmedListViewModel(
    confirmedRepository: ConfirmedScheduleRepository
) : ViewModel() {

    val schedules: StateFlow<List<CourtSchedule>> = confirmedRepository.observeAllSchedules()
        .map { it.sortedByDescending { schedule -> schedule.date.value } } // сортировка по дате (новые сверху)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}