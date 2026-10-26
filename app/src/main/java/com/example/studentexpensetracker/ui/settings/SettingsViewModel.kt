package com.example.studentexpensetracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val isDarkMode = userPreferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName = userPreferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Student")

    val allowanceDay = userPreferencesRepository.allowanceDay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val totalIncome = transactionRepository.getTotalIncome()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val isCustomBudget = userPreferencesRepository.customMonthlyBudget
        .map { it != null && it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val monthlyBudget = combine(
        userPreferencesRepository.customMonthlyBudget,
        transactionRepository.getTotalIncome()
    ) { custom, income ->
        val inc = income ?: 0.0
        when {
            custom != null && custom > 0 -> custom
            inc > 0 -> inc
            else -> 10000.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    fun setMonthlyBudget(amount: Double) {
        viewModelScope.launch {
            userPreferencesRepository.setMonthlyBudget(amount)
        }
    }

    fun resetMonthlyBudget() {
        viewModelScope.launch {
            userPreferencesRepository.clearMonthlyBudget()
        }
    }

    val notificationsEnabled = userPreferencesRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val survivalAnalysisDays = userPreferencesRepository.survivalAnalysisDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setUserName(name)
        }
    }

    fun setAllowanceDay(day: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setAllowanceDay(day)
        }
    }

    fun setSurvivalAnalysisDays(days: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setSurvivalAnalysisDays(days)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
        }
    }


    fun clearAllData() {
        viewModelScope.launch {
            transactionRepository.clearAllTransactions()
        }
    }
}
