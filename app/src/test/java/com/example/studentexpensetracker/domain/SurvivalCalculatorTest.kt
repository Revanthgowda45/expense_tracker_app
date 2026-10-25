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
            currentTimeMillis = getMillisForDayOfMonth(5) // 10 days until 15th
        )

        assertEquals(SurvivalStatus.SAFE, stats.status)
        assertEquals(20, stats.daysLeft)
        assertEquals(10, stats.daysUntilNextIncome)
        assertEquals(200.0, stats.safeDailyLimit, 0.01) // 2000 / 10
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
            currentTimeMillis = getMillisForDayOfMonth(5) // 10 days until 15th
        )

        assertEquals(SurvivalStatus.TIGHT, stats.status)
        assertEquals(10, stats.daysLeft)
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
            currentTimeMillis = getMillisForDayOfMonth(5)
        )

        assertEquals(SurvivalStatus.SAFE, stats.status)
        assertEquals(999, stats.daysLeft)
        assertEquals(100.0, stats.safeDailyLimit, 0.01) // 1000 / 10
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

    private fun getMillisForDayOfMonth(day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, day)
        return calendar.timeInMillis
    }
}
