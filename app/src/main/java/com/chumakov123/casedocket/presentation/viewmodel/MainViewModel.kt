package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.usecase.GetSettingsUseCase
import com.chumakov123.casedocket.domain.usecase.settings.UpdateSelectedTabUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSelectedTabUseCase: UpdateSelectedTabUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        viewModelScope.launch {
            getSettingsUseCase()
                .map { it.lastSelectedTab }
                .collect { tab ->
                    _selectedTab.value = tab
                }
        }
    }

    fun selectTab(index: Int) {
        viewModelScope.launch {
            updateSelectedTabUseCase(index)
        }
    }
}