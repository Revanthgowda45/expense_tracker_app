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
    val hasSufficientData: Boolean,
    val isIncomeDateSet: Boolean = true
)

object SurvivalCalculator {
    fun calculate(
        currentBalance: Double,
        recentExpensesSum: Double,
        daysAnalyzed: Int,
        allowanceDayOfMonth: Int?,
        todaySpent: Double = 0.0,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): SurvivalStats {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonthMaxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Check if allowance date is valid (1-31)
        val isIncomeDateSet = allowanceDayOfMonth != null && allowanceDayOfMonth in 1..31
        val daysUntilNextIncome = if (isIncomeDateSet) {
            val targetDay = allowanceDayOfMonth!!.coerceIn(1, 31)
            if (currentDay < targetDay) {
                targetDay - currentDay
            } else if (currentDay == targetDay) {
                // If today is allowance day, cycle to next month's allowance day
                val nextMonthCal = (calendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                }
                val nextMonthMaxDays = nextMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val targetDayNextMonth = minOf(targetDay, nextMonthMaxDays)
                (currentMonthMaxDays - currentDay) + targetDayNextMonth
            } else {
                val nextMonthCal = (calendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                }
                val nextMonthMaxDays = nextMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val targetDayNextMonth = minOf(targetDay, nextMonthMaxDays)
                (currentMonthMaxDays - currentDay) + targetDayNextMonth
            }
        } else {
            0
        }

        val effectiveDaysAnalyzed = max(daysAnalyzed, 1)
        val avgDailySpend = if (recentExpensesSum > 0) recentExpensesSum / effectiveDaysAnalyzed else 0.0

        // Edge Case 1: Balance is 0 or negative (Out of money / in debt)
        if (currentBalance <= 0) {
            return SurvivalStats(
                currentBalance = currentBalance,
                daysUntilNextIncome = daysUntilNextIncome,
                avgDailySpend = avgDailySpend,
                daysLeft = 0,
                safeDailyLimit = 0.0,
                todayRemaining = 0.0,
                status = SurvivalStatus.DANGER,
                hasSufficientData = true,
                isIncomeDateSet = isIncomeDateSet
            )
        }

        // Safe daily limit: currentBalance / daysUntilNextIncome (or 30 if not set)
        val safeDailyLimit = if (isIncomeDateSet && daysUntilNextIncome > 0) {
            currentBalance / daysUntilNextIncome
        } else {
            currentBalance / 30.0
        }

        // Remaining allowance for today
        val todayRemaining = (safeDailyLimit - todaySpent).coerceAtLeast(0.0)

        // Edge Case 2: No recent spending history or daysAnalyzed <= 0
        if (recentExpensesSum <= 0) {
            return SurvivalStats(
                currentBalance = currentBalance,
                daysUntilNextIncome = daysUntilNextIncome,
                avgDailySpend = 0.0,
                daysLeft = 999, // Practically infinite if no spending
                safeDailyLimit = safeDailyLimit,
                todayRemaining = todayRemaining,
                status = SurvivalStatus.SAFE,
                hasSufficientData = false,
                isIncomeDateSet = isIncomeDateSet
            )
        }

        val daysLeft = if (avgDailySpend > 0.01) {
            (currentBalance / avgDailySpend).toInt().coerceAtLeast(0)
        } else {
            999
        }

        val status = when {
            !isIncomeDateSet -> when {
                daysLeft >= 15 -> SurvivalStatus.SAFE
                daysLeft >= 7 -> SurvivalStatus.TIGHT
                else -> SurvivalStatus.DANGER
            }
            daysUntilNextIncome == 0 -> SurvivalStatus.SAFE
            daysLeft >= daysUntilNextIncome + 3 -> SurvivalStatus.SAFE
            daysLeft >= daysUntilNextIncome -> SurvivalStatus.TIGHT
            else -> SurvivalStatus.DANGER
        }

        return SurvivalStats(
            currentBalance = currentBalance,
            daysUntilNextIncome = daysUntilNextIncome,
            avgDailySpend = avgDailySpend,
            daysLeft = daysLeft,
            safeDailyLimit = safeDailyLimit,
            todayRemaining = todayRemaining,
            status = status,
            hasSufficientData = true,
            isIncomeDateSet = isIncomeDateSet
        )
    }
}

