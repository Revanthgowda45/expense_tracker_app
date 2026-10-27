package com.example.studentexpensetracker.ui.reports.mood;

public class TimeOfDayMoodPattern {
    private final String timeSlot;
    private final String timeRange;
    private final String dominantMood;
    private final String dominantEmoji;
    private final double totalAmount;
    private final int moodColor;

    public TimeOfDayMoodPattern(
            String timeSlot,
            String timeRange,
            String dominantMood,
            String dominantEmoji,
            double totalAmount,
            int moodColor) {
        this.timeSlot = timeSlot;
        this.timeRange = timeRange;
        this.dominantMood = dominantMood;
        this.dominantEmoji = dominantEmoji;
        this.totalAmount = totalAmount;
        this.moodColor = moodColor;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public String getDominantMood() {
        return dominantMood;
    }

    public String getDominantEmoji() {
        return dominantEmoji;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getMoodColor() {
        return moodColor;
    }
}
