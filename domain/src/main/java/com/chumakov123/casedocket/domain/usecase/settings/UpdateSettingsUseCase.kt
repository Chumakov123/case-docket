package com.chumakov123.casedocket.domain.usecase.settings

import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import com.chumakov123.casedocket.domain.usecase.notification.RescheduleNotificationsUseCase
import kotlinx.coroutines.flow.first

class UpdateSettingsUseCase(
    private val repository: SettingsRepository,
    private val rescheduleNotificationsUseCase: RescheduleNotificationsUseCase
) {
    suspend operator fun invoke(newSettings: Settings) {
        val oldSettings = repository.observeSettings().first()
        repository.updateSettings(newSettings)

        if (oldSettings.notificationMinutes != newSettings.notificationMinutes) {
            rescheduleNotificationsUseCase()
        }
    }
}