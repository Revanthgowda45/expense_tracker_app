package com.example.studentexpensetracker.domain;

import java.util.Calendar;

public class SurvivalCalculator {

    public static SurvivalStats calculate(
            double currentBalance,
            double recentExpensesSum,
            int daysAnalyzed,
            Integer allowanceDayOfMonth,
            double todaySpent,
            long currentTimeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonthMaxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        boolean isIncomeDateSet = allowanceDayOfMonth != null && allowanceDayOfMonth >= 1 && allowanceDayOfMonth <= 31;
        int daysUntilNextIncome;
        if (isIncomeDateSet) {
            int targetDay = Math.max(1, Math.min(31, allowanceDayOfMonth));
            if (currentDay < targetDay) {
                daysUntilNextIncome = targetDay - currentDay;
            } else {
                Calendar nextMonthCal = (Calendar) calendar.clone();
                nextMonthCal.add(Calendar.MONTH, 1);
                int nextMonthMaxDays = nextMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                int targetDayNextMonth = Math.min(targetDay, nextMonthMaxDays);
                daysUntilNextIncome = (currentMonthMaxDays - currentDay) + targetDayNextMonth;
            }
        } else {
            daysUntilNextIncome = 0;
        }

        int effectiveDaysAnalyzed = Math.max(daysAnalyzed, 1);
        double avgDailySpend = recentExpensesSum > 0 ? recentExpensesSum / effectiveDaysAnalyzed : 0.0;

        if (currentBalance <= 0) {
            return new SurvivalStats(
                    currentBalance,
                    daysUntilNextIncome,
                    avgDailySpend,
                    0,
                    0.0,
                    0.0,
                    SurvivalStatus.DANGER,
                    true,
                    isIncomeDateSet
            );
        }

        double safeDailyLimit;
        if (isIncomeDateSet && daysUntilNextIncome > 0) {
            safeDailyLimit = currentBalance / daysUntilNextIncome;
        } else {
            safeDailyLimit = currentBalance / 30.0;
        }

        double todayRemaining = Math.max(0.0, safeDailyLimit - todaySpent);

        if (recentExpensesSum <= 0) {
            return new SurvivalStats(
                    currentBalance,
                    daysUntilNextIncome,
                    0.0,
                    999,
                    safeDailyLimit,
                    todayRemaining,
                    SurvivalStatus.SAFE,
                    false,
                    isIncomeDateSet
            );
        }

        int daysLeft;
        if (avgDailySpend > 0.01) {
            daysLeft = Math.max(0, (int) (currentBalance / avgDailySpend));
        } else {
            daysLeft = 999;
        }

        SurvivalStatus status;
        if (!isIncomeDateSet) {
            if (daysLeft >= 15) {
                status = SurvivalStatus.SAFE;
            } else if (daysLeft >= 7) {
                status = SurvivalStatus.TIGHT;
            } else {
                status = SurvivalStatus.DANGER;
            }
        } else if (daysUntilNextIncome == 0) {
            status = SurvivalStatus.SAFE;
        } else if (daysLeft >= daysUntilNextIncome + 3) {
            status = SurvivalStatus.SAFE;
        } else if (daysLeft >= daysUntilNextIncome) {
            status = SurvivalStatus.TIGHT;
        } else {
            status = SurvivalStatus.DANGER;
        }

        return new SurvivalStats(
                currentBalance,
                daysUntilNextIncome,
                avgDailySpend,
                daysLeft,
                safeDailyLimit,
                todayRemaining,
                status,
                true,
                isIncomeDateSet
        );
    }

    public static SurvivalStats calculate(
            double currentBalance,
            double recentExpensesSum,
            int daysAnalyzed,
            Integer allowanceDayOfMonth) {
        return calculate(currentBalance, recentExpensesSum, daysAnalyzed, allowanceDayOfMonth, 0.0, System.currentTimeMillis());
    }

    public static SurvivalStats calculate(
            double currentBalance,
            double recentExpensesSum,
            int daysAnalyzed,
            Integer allowanceDayOfMonth,
            double todaySpent) {
        return calculate(currentBalance, recentExpensesSum, daysAnalyzed, allowanceDayOfMonth, todaySpent, System.currentTimeMillis());
    }
}
