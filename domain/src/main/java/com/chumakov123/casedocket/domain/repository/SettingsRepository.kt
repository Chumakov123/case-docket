package com.chumakov123.casedocket.domain.repository

import com.chumakov123.casedocket.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<Settings>
    suspend fun updateSettings(settings: Settings)
    suspend fun updateSelectedTab(tabIndex: Int)
}