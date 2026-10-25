package com.example.studentexpensetracker.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val ALLOWANCE_DAY_KEY = intPreferencesKey("allowance_day")

    val isDarkMode: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY]
    }

    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val allowanceDay: Flow<Int> = dataStore.data.map { preferences ->
        preferences[ALLOWANCE_DAY_KEY] ?: 1 // Default to 1st of the month
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun setAllowanceDay(day: Int) {
        dataStore.edit { preferences ->
            preferences[ALLOWANCE_DAY_KEY] = day.coerceIn(1, 31)
        }
    }
}
