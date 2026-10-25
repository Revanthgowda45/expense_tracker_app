package com.example.studentexpensetracker.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // Filter states
    val selectedFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"
    val searchQuery = MutableStateFlow("")

    // Raw transactions from DB
    private val rawTransactions = repository.getAllTransactions()

    // Combined Flow: Transactions + Filter + Search
    val uiState: StateFlow<TransactionsUiState> = combine(
        rawTransactions,
        selectedFilter,
        searchQuery
    ) { transactions, filter, query ->
        
        // 1. Apply Type Filter
        val typeFiltered = when (filter) {
            "INCOME" -> transactions.filter { it.type == "INCOME" }
            "EXPENSE" -> transactions.filter { it.type == "EXPENSE" }
            else -> transactions // "ALL"
        }

        // 2. Apply Search Filter
        val finalFiltered = if (query.isBlank()) {
            typeFiltered
        } else {
            typeFiltered.filter {
                (it.note?.contains(query, ignoreCase = true) == true) ||
                (it.amount.toString().contains(query))
            }
        }

        TransactionsUiState(
            transactions = finalFiltered,
            selectedFilter = filter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilter(filter: String) {
        selectedFilter.value = filter
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}

data class TransactionsUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val selectedFilter: String = "ALL",
    val searchQuery: String = ""
)
