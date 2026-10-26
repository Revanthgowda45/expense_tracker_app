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
    private val MONTHLY_BUDGET_KEY = androidx.datastore.preferences.core.doublePreferencesKey("monthly_budget")
    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    private val SURVIVAL_ANALYSIS_DAYS_KEY = intPreferencesKey("survival_analysis_days")
    private val LAST_SURVIVAL_NOTIFICATION_TIME_KEY = androidx.datastore.preferences.core.longPreferencesKey("last_survival_notification_time")

    val isDarkMode: Flow<Boolean?> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY]
    }

    val userName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val allowanceDay: Flow<Int> = dataStore.data.map { preferences ->
        preferences[ALLOWANCE_DAY_KEY] ?: 1 // Default to 1st of the month
    }

    val customMonthlyBudget: Flow<Double?> = dataStore.data.map { preferences ->
        preferences[MONTHLY_BUDGET_KEY]
    }

    val monthlyBudget: Flow<Double> = dataStore.data.map { preferences ->
        preferences[MONTHLY_BUDGET_KEY] ?: 10000.0 // Fallback if no income or budget
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true // Default enabled
    }

    val survivalAnalysisDays: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SURVIVAL_ANALYSIS_DAYS_KEY] ?: 14 // Default to 14 days
    }

    val lastSurvivalNotificationTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SURVIVAL_NOTIFICATION_TIME_KEY] ?: 0L
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

    suspend fun setMonthlyBudget(budget: Double) {
        dataStore.edit { preferences ->
            if (budget > 0) {
                preferences[MONTHLY_BUDGET_KEY] = budget
            } else {
                preferences.remove(MONTHLY_BUDGET_KEY)
            }
        }
    }

    suspend fun clearMonthlyBudget() {
        dataStore.edit { preferences ->
            preferences.remove(MONTHLY_BUDGET_KEY)
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setSurvivalAnalysisDays(days: Int) {
        dataStore.edit { preferences ->
            preferences[SURVIVAL_ANALYSIS_DAYS_KEY] = if (days == 7) 7 else 14
        }
    }

    suspend fun setLastSurvivalNotificationTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SURVIVAL_NOTIFICATION_TIME_KEY] = time
        }
    }
}

