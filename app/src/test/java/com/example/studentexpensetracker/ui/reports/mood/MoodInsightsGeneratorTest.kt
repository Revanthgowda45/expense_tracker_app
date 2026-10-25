package com.example.studentexpensetracker.ui.reports.mood

import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoodInsightsGeneratorTest {

    @Test
    fun `generate returns empty list when not enough data`() {
        val transactions = listOf(
            createTx(100.0, "Happy"),
            createTx(50.0, "Sad")
        )
        val insights = MoodInsightsGenerator.generate(transactions)
        assertTrue(insights.isEmpty())
    }

    @Test
    fun `generate identifies highest spending mood`() {
        val transactions = listOf(
            createTx(100.0, "Happy"),
            createTx(50.0, "Happy"),
            createTx(500.0, "Sad"),
            createTx(100.0, "Sad"),
            createTx(50.0, "Bored")
        )
        val insights = MoodInsightsGenerator.generate(transactions)
        
        // At least 5 transactions, so insights should generate
        assertTrue(insights.isNotEmpty())
        
        // Total Sad = 600, Total Happy = 150. Sad is highest spending mood.
        val highestSpendingInsight = insights.find { it.title == "Highest Spending Mood" }
        assertTrue(highestSpendingInsight?.description?.contains("Sad") == true)
    }

    @Test
    fun `generate identifies stress spending category`() {
        val transactions = listOf(
            createTx(100.0, "Stressed", "Food: Pizza"),
            createTx(50.0, "Stressed", "Food: Burger"),
            createTx(500.0, "Stressed", "Transport: Cab"),
            createTx(100.0, "Happy", "Food"),
            createTx(50.0, "Bored", "Entertainment")
        )
        
        val insights = MoodInsightsGenerator.generate(transactions)
        
        // Stressed has 2 Food, 1 Transport. Food should be the top category.
        val stressInsight = insights.find { it.title == "Stress Spending" }
        assertTrue(stressInsight?.description?.contains("Food") == true)
    }

    private fun createTx(amount: Double, mood: String, note: String = "Note"): TransactionEntity {
        return TransactionEntity(
            amount = amount,
            type = "EXPENSE",
            categoryId = 1,
            note = note,
            paymentMethod = "CASH",
            dateTime = 0L,
            isRecurring = false,
            recurrenceType = null,
            createdAt = 0L,
            mood = mood
        )
    }
}
