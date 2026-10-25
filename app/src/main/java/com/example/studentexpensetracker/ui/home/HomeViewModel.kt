package com.example.studentexpensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.data.repository.TransactionRepository
import com.example.studentexpensetracker.domain.SurvivalCalculator
import com.example.studentexpensetracker.domain.SurvivalStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: TransactionRepository,
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userName: StateFlow<String> = userPreferencesRepository.userName
        .map { it ?: "Student" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Student")

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .map { it.take(5) } // Only show the top 5 most recent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.getTotalIncome()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalExpense()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        
    val totalBalance: StateFlow<Double> = combine(
        totalIncome,
        totalExpense
    ) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Survival Meter Logic ---
    private val analysisDays = 14
    private val timeLimitMillis = System.currentTimeMillis() - (analysisDays * 24L * 60 * 60 * 1000)
    
    val recentExpensesSum: StateFlow<Double> = repository.getExpenseSumSince(timeLimitMillis)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val survivalStats: StateFlow<SurvivalStats?> = combine(
        totalBalance,
        recentExpensesSum,
        userPreferencesRepository.allowanceDay
    ) { balance, recentExpenses, allowanceDay ->
        SurvivalCalculator.calculate(
            currentBalance = balance,
            recentExpensesSum = recentExpenses,
            daysAnalyzed = analysisDays,
            allowanceDayOfMonth = allowanceDay
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
