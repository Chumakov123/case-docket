package com.chumakov123.casedocket.domain.usecase.settings

import com.chumakov123.casedocket.domain.repository.SettingsRepository

class UpdateSelectedTabUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(tabIndex: Int) {
        repository.updateSelectedTab(tabIndex)
    }
}