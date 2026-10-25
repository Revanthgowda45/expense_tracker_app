package com.example.studentexpensetracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun clearAllData() {
        viewModelScope.launch {
            transactionRepository.clearAllTransactions()
        }
    }
}
