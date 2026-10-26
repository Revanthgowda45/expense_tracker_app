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
        mood: String?,
        paymentMethod: String = "CASH",
        receiptImagePath: String? = null
    ): Boolean {
        val amountDouble = amount.toDoubleOrNull() ?: return false
        if (amountDouble <= 0) return false

        val selectedCat = category.ifBlank { if (type == "INCOME") "Other Income" else "Other Expense" }
        val finalNote = if (note.isNotBlank()) "$selectedCat: ${note.trim()}" else selectedCat

        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amountDouble,
                    type = type,
                    categoryId = 1,
                    note = finalNote,
                    paymentMethod = paymentMethod,
                    dateTime = if (dateMillis > 0) dateMillis else System.currentTimeMillis(),
                    isRecurring = false,
                    recurrenceType = null,
                    createdAt = System.currentTimeMillis(),
                    mood = mood,
                    receiptImagePath = receiptImagePath
                )
            )
        }
        return true
    }
}

