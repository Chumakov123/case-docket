package com.chumakov123.casedocket.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chumakov123.casedocket.domain.model.Settings
import com.chumakov123.casedocket.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val NOTIFICATION_MINUTES_KEY = intPreferencesKey("notification_minutes")
    }

    override fun observeSettings(): Flow<Settings> {
        return dataStore.data.map { preferences ->
            val savedLang = preferences[LANGUAGE_KEY]
            val effectiveLang = savedLang ?: computeDefaultLanguage()

            Settings(
                language = effectiveLang,
                theme = preferences[THEME_KEY] ?: "system",
                notificationMinutes = preferences[NOTIFICATION_MINUTES_KEY] ?: 10
            )
        }
    }

    override suspend fun updateSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = settings.language
            preferences[THEME_KEY] = settings.theme
            preferences[NOTIFICATION_MINUTES_KEY] = settings.notificationMinutes
        }
    }

    private fun computeDefaultLanguage(): String {
        return when (Locale.getDefault().language.lowercase()) {
            "ru" -> "ru"
            else -> "en"
        }
    }
}