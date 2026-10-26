package com.example.studentexpensetracker.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class SurvivalCalculatorTest {

    @Test
    fun `calculate returns SAFE when days left is well above days until income`() {
        // Setup: Current balance = 2000, daily spend = 100. Days left = 20.
        // If income is in 10 days, we are SAFE (20 > 10 + 3)
        val stats = SurvivalCalculator.calculate(
            currentBalance = 2000.0,
            recentExpensesSum = 1400.0, // 100/day
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            todaySpent = 50.0,
            currentTimeMillis = getMillisForDayOfMonth(5) // 10 days until 15th
        )

        assertEquals(SurvivalStatus.SAFE, stats.status)
        assertEquals(20, stats.daysLeft)
        assertEquals(10, stats.daysUntilNextIncome)
        assertEquals(200.0, stats.safeDailyLimit, 0.01) // 2000 / 10
        assertEquals(150.0, stats.todayRemaining, 0.01) // 200 - 50
    }

    @Test
    fun `calculate returns TIGHT when days left is exactly days until income`() {
        // Setup: Balance = 1000, spend = 100/day. Days left = 10.
        // Income is in 10 days. TIGHT.
        val stats = SurvivalCalculator.calculate(
            currentBalance = 1000.0,
            recentExpensesSum = 1400.0,
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            todaySpent = 20.0,
            currentTimeMillis = getMillisForDayOfMonth(5) // 10 days until 15th
        )

        assertEquals(SurvivalStatus.TIGHT, stats.status)
        assertEquals(10, stats.daysLeft)
        assertEquals(80.0, stats.todayRemaining, 0.01) // 100 - 20
    }

    @Test
    fun `calculate returns DANGER when days left is less than days until income`() {
        // Setup: Balance = 500, spend = 100/day. Days left = 5.
        // Income is in 10 days. DANGER.
        val stats = SurvivalCalculator.calculate(
            currentBalance = 500.0,
            recentExpensesSum = 1400.0,
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            todaySpent = 0.0,
            currentTimeMillis = getMillisForDayOfMonth(5)
        )

        assertEquals(SurvivalStatus.DANGER, stats.status)
        assertEquals(5, stats.daysLeft)
    }

    @Test
    fun `calculate handles zero recent expenses gracefully`() {
        // Setup: 0 expenses. Should not divide by zero. Days left should be 999 (infinite).
        val stats = SurvivalCalculator.calculate(
            currentBalance = 1000.0,
            recentExpensesSum = 0.0,
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            todaySpent = 0.0,
            currentTimeMillis = getMillisForDayOfMonth(5)
        )

        assertEquals(SurvivalStatus.SAFE, stats.status)
        assertEquals(999, stats.daysLeft)
        assertEquals(100.0, stats.safeDailyLimit, 0.01) // 1000 / 10
        assertEquals(100.0, stats.todayRemaining, 0.01)
    }

    @Test
    fun `calculate handles edge case when allowance day is today`() {
        val stats = SurvivalCalculator.calculate(
            currentBalance = 100.0,
            recentExpensesSum = 1400.0,
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            currentTimeMillis = getMillisForDayOfMonth(15) // Today is the 15th
        )

        // If today is the 15th, days until next income is the full month length (e.g. ~30 days)
        val calendar = Calendar.getInstance().apply { timeInMillis = getMillisForDayOfMonth(15) }
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        assertEquals(maxDays, stats.daysUntilNextIncome)
    }

    @Test
    fun `calculate returns DANGER with zero days left when current balance is zero or negative`() {
        val stats = SurvivalCalculator.calculate(
            currentBalance = 0.0,
            recentExpensesSum = 1400.0,
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            todaySpent = 50.0,
            currentTimeMillis = getMillisForDayOfMonth(5)
        )

        assertEquals(SurvivalStatus.DANGER, stats.status)
        assertEquals(0, stats.daysLeft)
        assertEquals(0.0, stats.safeDailyLimit, 0.001)
        assertEquals(0.0, stats.todayRemaining, 0.001)
    }

    @Test
    fun `calculate handles null or invalid allowance day correctly`() {
        val stats = SurvivalCalculator.calculate(
            currentBalance = 3000.0,
            recentExpensesSum = 1400.0, // 100/day -> 30 days left
            daysAnalyzed = 14,
            allowanceDayOfMonth = null,
            todaySpent = 25.0
        )

        assertEquals(false, stats.isIncomeDateSet)
        assertEquals(30, stats.daysLeft)
        assertEquals(100.0, stats.safeDailyLimit, 0.01) // 3000 / 30
        assertEquals(75.0, stats.todayRemaining, 0.01) // 100 - 25
        assertEquals(SurvivalStatus.SAFE, stats.status)
    }

    @Test
    fun `calculate clamps todayRemaining to zero if today spent exceeds safeDailyLimit`() {
        val stats = SurvivalCalculator.calculate(
            currentBalance = 1000.0,
            recentExpensesSum = 1400.0,
            daysAnalyzed = 14,
            allowanceDayOfMonth = 15,
            todaySpent = 250.0, // Exceeds safeDailyLimit of 100
            currentTimeMillis = getMillisForDayOfMonth(5) // 10 days until income -> limit = 100
        )

        assertEquals(100.0, stats.safeDailyLimit, 0.01)
        assertEquals(0.0, stats.todayRemaining, 0.001)
    }

    private fun getMillisForDayOfMonth(day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        return calendar.timeInMillis
    }
}

