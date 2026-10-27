package com.example.studentexpensetracker.ui.reports.mood;

public class MoodStat {
    private final String emoji;
    private final String mood;
    private final double totalAmount;
    private final int transactionCount;
    private final double avgAmount;
    private final float percentageOfMoodSpend;
    private final String topCategory;
    private final int color;

    public MoodStat(
            String emoji,
            String mood,
            double totalAmount,
            int transactionCount,
            double avgAmount,
            float percentageOfMoodSpend,
            String topCategory,
            int color) {
        this.emoji = emoji;
        this.mood = mood;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.avgAmount = avgAmount;
        this.percentageOfMoodSpend = percentageOfMoodSpend;
        this.topCategory = topCategory;
        this.color = color;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getMood() {
        return mood;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public double getAvgAmount() {
        return avgAmount;
    }

    public float getPercentageOfMoodSpend() {
        return percentageOfMoodSpend;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public int getColor() {
        return color;
    }
}
