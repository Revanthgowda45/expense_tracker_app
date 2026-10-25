package com.example.studentexpensetracker.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {

    // Same category grouping logic as reports
    val expensesByCategory: StateFlow<Map<String, Double>> = repository.getAllTransactions()
        .map { transactions ->
            transactions
                .filter { it.type == "EXPENSE" }
                .groupBy { 
                    it.note?.substringBefore(":")?.trim() ?: "Uncategorized" 
                }
                .mapValues { entry ->
                    entry.value.sumOf { it.amount }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalExpense: StateFlow<Double> = repository.getTotalExpense()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
}
