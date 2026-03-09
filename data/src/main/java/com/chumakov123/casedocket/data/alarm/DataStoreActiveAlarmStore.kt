package com.chumakov123.casedocket.data.alarm

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreActiveAlarmStore(
    private val dataStore: DataStore<Preferences>
) : ActiveAlarmStore {

    companion object {
        private val ALARM_REQUEST_CODES_KEY = stringSetPreferencesKey("active_alarm_request_codes")
    }

    override suspend fun addRequestCode(requestCode: Int) {
        dataStore.edit { preferences ->
            val currentSet = preferences[ALARM_REQUEST_CODES_KEY] ?: emptySet()
            preferences[ALARM_REQUEST_CODES_KEY] = currentSet + requestCode.toString()
        }
    }

    override suspend fun getAllRequestCodes(): List<Int> {
        return dataStore.data.map { preferences ->
            preferences[ALARM_REQUEST_CODES_KEY]?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        }.first()
    }

    override suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.remove(ALARM_REQUEST_CODES_KEY)
        }
    }
}