package com.example.studentexpensetracker.ui.reports.mood;

import java.util.Collections;
import java.util.List;

public class MoodAnalytics {
    private final int totalMoodTransactions;
    private final double totalMoodSpend;
    private final List<MoodStat> moodStats;
    private final MoodStat highestSpendMood;
    private final MoodStat highestAvgMood;
    private final List<MoodInsight> insights;
    private final List<TimeOfDayMoodPattern> timeOfDayPatterns;
    private final boolean isThresholdReached;
    private final int thresholdCount;
    private final int remainingToThreshold;

    public static final MoodAnalytics EMPTY = new MoodAnalytics(
            0,
            0.0,
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            false,
            5,
            5
    );

    public MoodAnalytics(
            int totalMoodTransactions,
            double totalMoodSpend,
            List<MoodStat> moodStats,
            MoodStat highestSpendMood,
            MoodStat highestAvgMood,
            List<MoodInsight> insights,
            List<TimeOfDayMoodPattern> timeOfDayPatterns,
            boolean isThresholdReached,
            int thresholdCount,
            int remainingToThreshold) {
        this.totalMoodTransactions = totalMoodTransactions;
        this.totalMoodSpend = totalMoodSpend;
        this.moodStats = moodStats != null ? moodStats : Collections.emptyList();
        this.highestSpendMood = highestSpendMood;
        this.highestAvgMood = highestAvgMood;
        this.insights = insights != null ? insights : Collections.emptyList();
        this.timeOfDayPatterns = timeOfDayPatterns != null ? timeOfDayPatterns : Collections.emptyList();
        this.isThresholdReached = isThresholdReached;
        this.thresholdCount = thresholdCount;
        this.remainingToThreshold = remainingToThreshold;
    }

    public int getTotalMoodTransactions() {
        return totalMoodTransactions;
    }

    public double getTotalMoodSpend() {
        return totalMoodSpend;
    }

    public List<MoodStat> getMoodStats() {
        return moodStats;
    }

    public MoodStat getHighestSpendMood() {
        return highestSpendMood;
    }

    public MoodStat getHighestAvgMood() {
        return highestAvgMood;
    }

    public List<MoodInsight> getInsights() {
        return insights;
    }

    public List<TimeOfDayMoodPattern> getTimeOfDayPatterns() {
        return timeOfDayPatterns;
    }

    public boolean isThresholdReached() {
        return isThresholdReached;
    }

    public int getThresholdCount() {
        return thresholdCount;
    }

    public int getRemainingToThreshold() {
        return remainingToThreshold;
    }
}
