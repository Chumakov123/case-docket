package com.chumakov123.casedocket.domain.usecase.settings

import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.repository.SettingsRepository

class UpdateSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: Settings) {
        repository.updateSettings(settings)
    }
}