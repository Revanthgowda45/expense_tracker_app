package com.example.studentexpensetracker.ui.reports.mood;

public class WeeklyMoodSummary {
    private final String dominantMood;
    private final String dominantEmoji;
    private final int dominantColor;
    private final double totalWeeklySpend;
    private final int moodTransactionCount;
    private final int positivePercentage;
    private final String headline;
    private final String summaryInsight;

    public WeeklyMoodSummary(
            String dominantMood,
            String dominantEmoji,
            int dominantColor,
            double totalWeeklySpend,
            int moodTransactionCount,
            int positivePercentage,
            String headline,
            String summaryInsight) {
        this.dominantMood = dominantMood;
        this.dominantEmoji = dominantEmoji;
        this.dominantColor = dominantColor;
        this.totalWeeklySpend = totalWeeklySpend;
        this.moodTransactionCount = moodTransactionCount;
        this.positivePercentage = positivePercentage;
        this.headline = headline;
        this.summaryInsight = summaryInsight;
    }

    public String getDominantMood() {
        return dominantMood;
    }

    public String getDominantEmoji() {
        return dominantEmoji;
    }

    public int getDominantColor() {
        return dominantColor;
    }

    public double getTotalWeeklySpend() {
        return totalWeeklySpend;
    }

    public int getMoodTransactionCount() {
        return moodTransactionCount;
    }

    public int getPositivePercentage() {
        return positivePercentage;
    }

    public String getHeadline() {
        return headline;
    }

    public String getSummaryInsight() {
        return summaryInsight;
    }
}
