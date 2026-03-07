package com.chumakov123.casedocket.domain.usecase.settings

import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase

class UpdateSettingsUseCase(
    private val repository: SettingsRepository,
    private val rescheduleNotificationsUseCase: RescheduleNotificationsUseCase
) {
    suspend operator fun invoke(settings: Settings) {
        repository.updateSettings(settings)
        rescheduleNotificationsUseCase()
    }
}