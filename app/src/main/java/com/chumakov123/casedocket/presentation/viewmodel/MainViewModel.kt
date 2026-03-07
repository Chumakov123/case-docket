package com.chumakov123.casedocket.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.usecase.GetSettingsUseCase
import com.chumakov123.casedocket.domain.usecase.settings.UpdateSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    val selectedTab: StateFlow<Int> = getSettingsUseCase()
        .map { it.lastSelectedTab }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun selectTab(index: Int) {
        viewModelScope.launch {
            val currentSettings = getSettingsUseCase().stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = Settings("", "system", 10, 0)
            ).value

            val updatedSettings = currentSettings.copy(lastSelectedTab = index)
            updateSettingsUseCase(updatedSettings)
        }
    }
}