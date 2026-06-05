package com.chumakov123.casedocket.data.alarm

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreNotifiedAlarmStore(
    private val dataStore: DataStore<Preferences>
) : NotifiedAlarmStore {

    companion object {
        private val NOTIFIED_ALARM_KEY = stringSetPreferencesKey("notified_alarm_with_timestamps")
        private const val DELIMITER = ":"
    }

    override suspend fun markAsNotified(requestCode: Int) {
        val entry = "$requestCode$DELIMITER${System.currentTimeMillis()}"
        dataStore.edit { preferences ->
            val currentSet = preferences[NOTIFIED_ALARM_KEY] ?: emptySet()
            val filteredSet = currentSet.filter { !it.startsWith("$requestCode$DELIMITER") }.toSet()
            preferences[NOTIFIED_ALARM_KEY] = filteredSet + entry
        }
    }

    override suspend fun isNotified(requestCode: Int): Boolean {
        return dataStore.data.map { preferences ->
            val currentSet = preferences[NOTIFIED_ALARM_KEY] ?: emptySet()
            currentSet.any { it.startsWith("$requestCode$DELIMITER") }
        }.first()
    }

    override suspend fun clearOld(thresholdMillis: Long) {
        val now = System.currentTimeMillis()
        dataStore.edit { preferences ->
            val currentSet = preferences[NOTIFIED_ALARM_KEY] ?: emptySet()
            val filteredSet = currentSet.filter { entry ->
                val parts = entry.split(DELIMITER)
                if (parts.size == 2) {
                    val timestamp = parts[1].toLongOrNull() ?: 0L
                    (now - timestamp) < thresholdMillis
                } else {
                    false
                }
            }.toSet()
            preferences[NOTIFIED_ALARM_KEY] = filteredSet
        }
    }
}