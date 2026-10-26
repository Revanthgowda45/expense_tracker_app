package com.example.studentexpensetracker.ui.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.studentexpensetracker.domain.scanner.ReceiptStorageManager

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L

    val transaction: StateFlow<TransactionEntity?> = repository.getTransactionById(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateTransaction(
        amount: String,
        type: String,
        category: String,
        dateMillis: Long,
        note: String,
        mood: String?,
        paymentMethod: String = "CASH",
        receiptImagePath: String? = null
    ): Boolean {
        val current = transaction.value ?: return false
        val amountDouble = amount.toDoubleOrNull() ?: return false
        if (amountDouble <= 0) return false

        val selectedCat = category.ifBlank { if (type == "INCOME") "Other Income" else "Other Expense" }
        val finalNote = if (note.isNotBlank()) "$selectedCat: ${note.trim()}" else selectedCat

        viewModelScope.launch {
            repository.updateTransaction(
                current.copy(
                    amount = amountDouble,
                    type = type,
                    note = finalNote,
                    dateTime = if (dateMillis > 0) dateMillis else current.dateTime,
                    mood = mood,
                    paymentMethod = paymentMethod,
                    receiptImagePath = receiptImagePath
                )
            )
        }
        return true
    }

    fun deleteTransaction() {
        val current = transaction.value ?: return
        viewModelScope.launch {
            ReceiptStorageManager.deleteReceiptImage(current.receiptImagePath)
            repository.deleteTransaction(current)
        }
    }
}
