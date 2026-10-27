package com.example.studentexpensetracker.domain;

public class SurvivalStats {
    private final double currentBalance;
    private final int daysUntilNextIncome;
    private final double avgDailySpend;
    private final int daysLeft;
    private final double safeDailyLimit;
    private final double todayRemaining;
    private final SurvivalStatus status;
    private final boolean hasSufficientData;
    private final boolean isIncomeDateSet;

    public SurvivalStats(
            double currentBalance,
            int daysUntilNextIncome,
            double avgDailySpend,
            int daysLeft,
            double safeDailyLimit,
            double todayRemaining,
            SurvivalStatus status,
            boolean hasSufficientData,
            boolean isIncomeDateSet) {
        this.currentBalance = currentBalance;
        this.daysUntilNextIncome = daysUntilNextIncome;
        this.avgDailySpend = avgDailySpend;
        this.daysLeft = daysLeft;
        this.safeDailyLimit = safeDailyLimit;
        this.todayRemaining = todayRemaining;
        this.status = status;
        this.hasSufficientData = hasSufficientData;
        this.isIncomeDateSet = isIncomeDateSet;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public int getDaysUntilNextIncome() {
        return daysUntilNextIncome;
    }

    public double getAvgDailySpend() {
        return avgDailySpend;
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public double getSafeDailyLimit() {
        return safeDailyLimit;
    }

    public double getTodayRemaining() {
        return todayRemaining;
    }

    public SurvivalStatus getStatus() {
        return status;
    }

    public boolean hasSufficientData() {
        return hasSufficientData;
    }

    public boolean isIncomeDateSet() {
        return isIncomeDateSet;
    }
}
