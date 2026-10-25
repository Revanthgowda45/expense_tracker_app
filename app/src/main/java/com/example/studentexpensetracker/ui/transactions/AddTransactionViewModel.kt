package com.example.studentexpensetracker.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    
    fun saveTransaction(
        amount: String, 
        type: String, 
        category: String, 
        dateMillis: Long, 
        note: String,
        mood: String?
    ) {
        val amountDouble = amount.toDoubleOrNull() ?: 0.0
        if (amountDouble <= 0) return

        val finalNote = if (note.isNotBlank()) "$category: $note" else category

        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amountDouble,
                    type = type,
                    categoryId = 1,
                    note = finalNote,
                    paymentMethod = "CASH",
                    dateTime = dateMillis,
                    isRecurring = false,
                    recurrenceType = null,
                    createdAt = System.currentTimeMillis(),
                    mood = mood
                )
            )
        }
    }
}
