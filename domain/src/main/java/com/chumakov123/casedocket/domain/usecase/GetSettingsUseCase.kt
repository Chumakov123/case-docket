package com.chumakov123.casedocket.domain.usecase

import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> = repository.observeSettings()
}