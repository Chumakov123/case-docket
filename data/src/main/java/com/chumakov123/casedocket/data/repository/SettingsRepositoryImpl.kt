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
        private val LAST_TAB_KEY = intPreferencesKey("last_selected_tab")
    }

    override fun observeSettings(): Flow<Settings> =
        dataStore.data.map { preferences ->
            val savedLang = preferences[LANGUAGE_KEY]
            val effectiveLang = savedLang ?: computeDefaultLanguage()

            Settings(
                language = effectiveLang,
                theme = preferences[THEME_KEY] ?: "system",
                notificationMinutes = preferences[NOTIFICATION_MINUTES_KEY] ?: 10,
                lastSelectedTab = preferences[LAST_TAB_KEY] ?: 0
            )
        }

    override suspend fun updateSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = settings.language
            preferences[THEME_KEY] = settings.theme
            preferences[NOTIFICATION_MINUTES_KEY] = settings.notificationMinutes
            preferences[LAST_TAB_KEY] = settings.lastSelectedTab
        }
    }

    override suspend fun updateSelectedTab(tabIndex: Int) {
        dataStore.edit { preferences ->
            val currentLang = preferences[LANGUAGE_KEY] ?: computeDefaultLanguage()
            val currentTheme = preferences[THEME_KEY] ?: "system"
            val currentNotifications = preferences[NOTIFICATION_MINUTES_KEY] ?: 10

            preferences[LANGUAGE_KEY] = currentLang
            preferences[THEME_KEY] = currentTheme
            preferences[NOTIFICATION_MINUTES_KEY] = currentNotifications
            preferences[LAST_TAB_KEY] = tabIndex
        }
    }

    private fun computeDefaultLanguage(): String {
        return when (Locale.getDefault().language.lowercase()) {
            "ru" -> "ru"
            else -> "en"
        }
    }
}