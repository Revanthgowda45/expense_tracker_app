package com.example.studentexpensetracker.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.repository.TransactionRepository
import com.example.studentexpensetracker.ui.reports.mood.MoodInsight
import com.example.studentexpensetracker.ui.reports.mood.MoodInsightsGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {

    // Only get expenses for the pie chart
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

    val moodInsights: StateFlow<List<MoodInsight>> = repository.getAllTransactions()
        .map { transactions ->
            MoodInsightsGenerator.generate(transactions)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
