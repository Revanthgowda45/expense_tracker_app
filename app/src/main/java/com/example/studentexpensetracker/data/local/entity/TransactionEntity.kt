package com.example.studentexpensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val categoryId: Long,
    val note: String?,
    val paymentMethod: String,
    val dateTime: Long,
    val isRecurring: Boolean,
    val recurrenceType: String?,
    val createdAt: Long,
    val mood: String? = null, // New field for Phase B: Mood Insights (e.g. "Happy", "Sad")
    val receiptImagePath: String? = null // New field for Phase E: Receipt Scanner (path to local receipt image)
)
