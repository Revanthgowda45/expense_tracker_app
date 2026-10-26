package com.example.studentexpensetracker.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    repository: TransactionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val overallBudget: StateFlow<Double> = userPreferencesRepository.monthlyBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    val expensesByCategory: StateFlow<Map<String, Double>> = repository.getAllTransactions()
        .map { transactions ->
            transactions
                .filter { it.type == "EXPENSE" }
                .groupBy { 
                    val cat = it.note?.substringBefore(":")?.trim()
                    if (!cat.isNullOrBlank()) cat else "Other Expense" 
                }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalExpense: StateFlow<Double> = repository.getTotalExpense()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun updateOverallBudget(amount: Double) {
        if (amount > 0) {
            viewModelScope.launch {
                userPreferencesRepository.setMonthlyBudget(amount)
            }
        }
    }
}

