package com.example.studentexpensetracker.domain

import java.util.Calendar
import kotlin.math.max

enum class SurvivalStatus {
    SAFE, TIGHT, DANGER
}

data class SurvivalStats(
    val currentBalance: Double,
    val daysUntilNextIncome: Int,
    val avgDailySpend: Double,
    val daysLeft: Int,
    val safeDailyLimit: Double,
    val todayRemaining: Double,
    val status: SurvivalStatus,
    val hasSufficientData: Boolean
)

object SurvivalCalculator {
    fun calculate(
        currentBalance: Double,
        recentExpensesSum: Double,
        daysAnalyzed: Int,
        allowanceDayOfMonth: Int,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): SurvivalStats {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonthMaxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Calculate days until next income
        val daysUntilNextIncome = if (currentDay < allowanceDayOfMonth) {
            allowanceDayOfMonth - currentDay
        } else {
            (currentMonthMaxDays - currentDay) + allowanceDayOfMonth
        }

        if (recentExpensesSum <= 0 || daysAnalyzed <= 0 || currentBalance <= 0) {
            val safeLimit = if (daysUntilNextIncome > 0) currentBalance / daysUntilNextIncome else currentBalance
            return SurvivalStats(
                currentBalance = currentBalance,
                daysUntilNextIncome = daysUntilNextIncome,
                avgDailySpend = 0.0,
                daysLeft = 999, // Practically infinite if no spending
                safeDailyLimit = safeLimit,
                todayRemaining = safeLimit,
                status = SurvivalStatus.SAFE,
                hasSufficientData = false
            )
        }

        val avgDailySpend = recentExpensesSum / daysAnalyzed
        val daysLeft = (currentBalance / avgDailySpend).toInt()
        val safeDailyLimit = currentBalance / max(daysUntilNextIncome, 1)

        val status = when {
            daysLeft > daysUntilNextIncome + 3 -> SurvivalStatus.SAFE
            daysLeft >= daysUntilNextIncome -> SurvivalStatus.TIGHT
            else -> SurvivalStatus.DANGER
        }

        return SurvivalStats(
            currentBalance = currentBalance,
            daysUntilNextIncome = daysUntilNextIncome,
            avgDailySpend = avgDailySpend,
            daysLeft = daysLeft,
            safeDailyLimit = safeDailyLimit,
            todayRemaining = safeDailyLimit, // Simplified for now
            status = status,
            hasSufficientData = true
        )
    }
}
