package com.example.studentexpensetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.data.repository.TransactionRepository
import com.example.studentexpensetracker.domain.SurvivalCalculator
import com.example.studentexpensetracker.domain.SurvivalStats
import com.example.studentexpensetracker.ui.reports.mood.MoodInsightsGenerator
import com.example.studentexpensetracker.ui.reports.mood.WeeklyMoodSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
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

    val isCustomBudget: StateFlow<Boolean> = userPreferencesRepository.customMonthlyBudget
        .map { it != null && it > 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val monthlyBudget: StateFlow<Double> = combine(
        userPreferencesRepository.customMonthlyBudget,
        totalIncome
    ) { customBudget, income ->
        when {
            customBudget != null && customBudget > 0 -> customBudget
            income > 0 -> income
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

    // --- Survival Meter Logic ---
    val survivalAnalysisDays: StateFlow<Int> = userPreferencesRepository.survivalAnalysisDays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    private fun getStartOfDayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val todaySpent: StateFlow<Double> = repository.getAllTransactions()
        .map { transactions ->
            val startOfDay = getStartOfDayMillis()
            transactions
                .filter { it.type == "EXPENSE" && it.dateTime >= startOfDay }
                .sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recentExpensesSum: StateFlow<Double> = combine(
        repository.getAllTransactions(),
        survivalAnalysisDays
    ) { transactions, days ->
        val limit = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000)
        transactions
            .filter { it.type == "EXPENSE" && it.dateTime >= limit }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val survivalStats: StateFlow<SurvivalStats?> = combine(
        totalBalance,
        recentExpensesSum,
        survivalAnalysisDays,
        userPreferencesRepository.allowanceDay,
        todaySpent
    ) { balance, recentExpenses, days, allowanceDay, todayExpense ->
        SurvivalCalculator.calculate(
            currentBalance = balance,
            recentExpensesSum = recentExpenses,
            daysAnalyzed = days,
            allowanceDayOfMonth = allowanceDay,
            todaySpent = todayExpense
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weeklyMoodSummary: StateFlow<WeeklyMoodSummary?> = repository.getAllTransactions()
        .map { transactions ->
            MoodInsightsGenerator.generateWeeklySummary(transactions)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

