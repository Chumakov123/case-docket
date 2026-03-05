package com.chumakov123.casedocket.presentation.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.usecase.GetSettingsUseCase
import com.chumakov123.casedocket.domain.usecase.settings.UpdateSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {

    val settings: StateFlow<Settings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings("ru", "system", 10)
        )

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            val current = settings.value
            updateSettingsUseCase(current.copy(language = language))

            val localeList = when (language) {
                "ru" -> LocaleListCompat.forLanguageTags("ru")
                "en" -> LocaleListCompat.forLanguageTags("en")
                else -> LocaleListCompat.getEmptyLocaleList()
            }
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            val current = settings.value
            updateSettingsUseCase(current.copy(theme = theme))
        }
    }

    fun updateNotificationMinutes(minutes: Int) {
        viewModelScope.launch {
            val current = settings.value
            updateSettingsUseCase(current.copy(notificationMinutes = minutes))
        }
    }
}