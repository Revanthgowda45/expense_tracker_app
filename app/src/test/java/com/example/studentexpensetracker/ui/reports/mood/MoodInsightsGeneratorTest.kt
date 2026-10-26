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

    @Test
    fun `analyze returns threshold not reached when fewer than 5 transactions`() {
        val transactions = listOf(
            createTx(100.0, "Happy"),
            createTx(50.0, "Sad")
        )
        val analytics = MoodInsightsGenerator.analyze(transactions)
        assertTrue(!analytics.isThresholdReached)
        assertEquals(2, analytics.totalMoodTransactions)
        assertEquals(3, analytics.remainingToThreshold)
    }

    @Test
    fun `analyze computes correct breakdown and top spending mood`() {
        val transactions = listOf(
            createTx(200.0, "Happy", "Food: Lunch"),
            createTx(300.0, "Happy", "Food: Dinner"),
            createTx(1000.0, "Stressed", "Shopping: Clothes"),
            createTx(500.0, "Stressed", "Food: Snacks"),
            createTx(100.0, "Bored", "Entertainment: Movie")
        )
        val analytics = MoodInsightsGenerator.analyze(transactions)
        assertTrue(analytics.isThresholdReached)
        assertEquals(5, analytics.totalMoodTransactions)
        assertEquals(2100.0, analytics.totalMoodSpend, 0.01)
        assertEquals("Stressed", analytics.highestSpendMood?.mood)
        assertEquals(1500.0, analytics.highestSpendMood?.totalAmount ?: 0.0, 0.01)
        assertTrue(analytics.insights.isNotEmpty())
    }

    @Test
    fun `generateWeeklySummary calculates dominant mood and positive ratio`() {
        val now = System.currentTimeMillis()
        val transactions = listOf(
            createTx(100.0, "Happy", dateTime = now),
            createTx(200.0, "Celebrating", dateTime = now),
            createTx(100.0, "Stressed", dateTime = now)
        )
        val weeklySummary = MoodInsightsGenerator.generateWeeklySummary(transactions)
        assertTrue(weeklySummary != null)
        assertEquals(3, weeklySummary?.moodTransactionCount)
        assertEquals(400.0, weeklySummary?.totalWeeklySpend ?: 0.0, 0.01)
        assertEquals(75, weeklySummary?.positivePercentage) // 300 / 400 = 75%
    }

    private fun createTx(amount: Double, mood: String, note: String = "Note", dateTime: Long = 0L): TransactionEntity {
        return TransactionEntity(
            amount = amount,
            type = "EXPENSE",
            categoryId = 1,
            note = note,
            paymentMethod = "CASH",
            dateTime = if (dateTime > 0) dateTime else System.currentTimeMillis(),
            isRecurring = false,
            recurrenceType = null,
            createdAt = 0L,
            mood = mood
        )
    }
}
