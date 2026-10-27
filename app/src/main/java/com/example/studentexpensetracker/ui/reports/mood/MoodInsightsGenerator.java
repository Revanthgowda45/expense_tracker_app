package com.example.studentexpensetracker.ui.reports.mood;

import com.example.studentexpensetracker.data.local.entity.TransactionEntity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MoodInsightsGenerator {

    private static final Map<String, String> MOOD_EMOJIS = new HashMap<>();

    static {
        MOOD_EMOJIS.put("Happy", "😊");
        MOOD_EMOJIS.put("Neutral", "😐");
        MOOD_EMOJIS.put("Stressed", "😟");
        MOOD_EMOJIS.put("Bored", "🥱");
        MOOD_EMOJIS.put("Sad", "😢");
        MOOD_EMOJIS.put("Celebrating", "🎉");
    }

    public static String getMoodEmoji(String mood) {
        if (mood == null) return "💭";
        for (Map.Entry<String, String> entry : MOOD_EMOJIS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(mood)) {
                return entry.getValue();
            }
        }
        String lower = mood.toLowerCase(Locale.ROOT);
        switch (lower) {
            case "happy": return "😊";
            case "neutral": return "😐";
            case "stressed": return "😟";
            case "bored": return "🥱";
            case "sad": return "😢";
            case "celebrating": return "🎉";
            default: return "💭";
        }
    }

    public static int getMoodColor(String mood) {
        if (mood == null) return 0xFF94A3B8;
        switch (mood.toLowerCase(Locale.ROOT)) {
            case "happy": return 0xFF10B981; // Emerald
            case "celebrating": return 0xFF8B5CF6; // Purple
            case "neutral": return 0xFF64748B; // Slate
            case "bored": return 0xFFF59E0B; // Amber
            case "stressed": return 0xFFEF4444; // Red
            case "sad": return 0xFF3B82F6; // Blue
            default: return 0xFF94A3B8;
        }
    }

    public static List<MoodInsight> generate(List<TransactionEntity> transactions) {
        List<TransactionEntity> moodTxs = filterMoodExpenses(transactions);
        if (moodTxs.size() < 5) {
            return Collections.emptyList();
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        List<MoodInsight> insights = new ArrayList<>();

        // 1. Total spent by mood
        Map<String, Double> spentByMood = new HashMap<>();
        for (TransactionEntity tx : moodTxs) {
            String m = tx.getMood();
            spentByMood.put(m, spentByMood.getOrDefault(m, 0.0) + tx.getAmount());
        }

        Map.Entry<String, Double> topMood = null;
        for (Map.Entry<String, Double> entry : spentByMood.entrySet()) {
            if (topMood == null || entry.getValue() > topMood.getValue()) {
                topMood = entry;
            }
        }

        if (topMood != null && topMood.getValue() > 0) {
            insights.add(new MoodInsight(
                    "Highest Spending Mood",
                    "You spend the most money when you are feeling " + topMood.getKey() + ".",
                    getMoodEmoji(topMood.getKey())
            ));
        }

        // 2. Average spend by mood
        Map<String, List<TransactionEntity>> moodGroups = groupByMood(moodTxs);
        String highestAvgMoodName = null;
        double highestAvgVal = 0.0;

        for (Map.Entry<String, List<TransactionEntity>> entry : moodGroups.entrySet()) {
            double sum = 0.0;
            for (TransactionEntity tx : entry.getValue()) sum += tx.getAmount();
            double avg = sum / entry.getValue().size();
            if (avg > highestAvgVal) {
                highestAvgVal = avg;
                highestAvgMoodName = entry.getKey();
            }
        }

        if (highestAvgMoodName != null && highestAvgVal > 0) {
            insights.add(new MoodInsight(
                    "Most Expensive Habit",
                    "Your average transaction is highest (" + formatter.format(highestAvgVal) + ") when you are " + highestAvgMoodName + ".",
                    getMoodEmoji(highestAvgMoodName)
            ));
        }

        // 3. Stressed category
        List<TransactionEntity> stressedTxs = new ArrayList<>();
        for (TransactionEntity tx : moodTxs) {
            if ("Stressed".equalsIgnoreCase(tx.getMood())) {
                stressedTxs.add(tx);
            }
        }

        if (!stressedTxs.isEmpty()) {
            Map<String, Integer> catCounts = new HashMap<>();
            for (TransactionEntity tx : stressedTxs) {
                String cat = extractCategory(tx.getNote());
                catCounts.put(cat, catCounts.getOrDefault(cat, 0) + 1);
            }
            String topStressedCat = null;
            int maxCatCount = 0;
            for (Map.Entry<String, Integer> entry : catCounts.entrySet()) {
                if (entry.getValue() > maxCatCount) {
                    maxCatCount = entry.getValue();
                    topStressedCat = entry.getKey();
                }
            }

            if (topStressedCat != null) {
                insights.add(new MoodInsight(
                        "Stress Spending",
                        "When stressed, you frequently spend on " + topStressedCat + ".",
                        "😟"
                ));
            }
        }

        return insights;
    }

    public static MoodAnalytics analyze(List<TransactionEntity> transactions) {
        List<TransactionEntity> moodTxs = filterMoodExpenses(transactions);
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        int threshold = 5;

        double totalMoodSpend = 0.0;
        for (TransactionEntity tx : moodTxs) totalMoodSpend += tx.getAmount();

        if (moodTxs.size() < threshold) {
            return new MoodAnalytics(
                    moodTxs.size(),
                    totalMoodSpend,
                    Collections.emptyList(),
                    null,
                    null,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    false,
                    threshold,
                    threshold - moodTxs.size()
            );
        }

        double overallAvg = moodTxs.isEmpty() ? 0.0 : totalMoodSpend / moodTxs.size();

        Map<String, List<TransactionEntity>> moodGroups = groupByMood(moodTxs);
        List<MoodStat> moodStats = new ArrayList<>();

        for (Map.Entry<String, List<TransactionEntity>> entry : moodGroups.entrySet()) {
            String moodName = entry.getKey();
            List<TransactionEntity> txs = entry.getValue();
            double total = 0.0;
            for (TransactionEntity tx : txs) total += tx.getAmount();
            int count = txs.size();
            double avg = count > 0 ? total / count : 0.0;
            float pct = totalMoodSpend > 0 ? (float) ((total / totalMoodSpend) * 100.0) : 0f;

            Map<String, Integer> catCounts = new HashMap<>();
            for (TransactionEntity tx : txs) {
                String cat = extractCategory(tx.getNote());
                catCounts.put(cat, catCounts.getOrDefault(cat, 0) + 1);
            }
            String topCat = "General";
            int maxC = 0;
            for (Map.Entry<String, Integer> ce : catCounts.entrySet()) {
                if (ce.getValue() > maxC) {
                    maxC = ce.getValue();
                    topCat = ce.getKey();
                }
            }

            moodStats.add(new MoodStat(
                    getMoodEmoji(moodName),
                    moodName,
                    total,
                    count,
                    avg,
                    pct,
                    topCat,
                    getMoodColor(moodName)
            ));
        }

        // Sort descending by totalAmount
        Collections.sort(moodStats, (a, b) -> Double.compare(b.getTotalAmount(), a.getTotalAmount()));

        MoodStat highestSpendMood = moodStats.isEmpty() ? null : moodStats.get(0);

        MoodStat highestAvgMood = null;
        for (MoodStat stat : moodStats) {
            if (highestAvgMood == null || stat.getAvgAmount() > highestAvgMood.getAvgAmount()) {
                highestAvgMood = stat;
            }
        }

        // Time of Day Patterns
        List<TimeOfDayMoodPattern> timePatterns = new ArrayList<>();
        timePatterns.add(calcTimePattern("Morning", "6 AM - 12 PM", 6, 11, moodTxs));
        timePatterns.add(calcTimePattern("Afternoon", "12 PM - 5 PM", 12, 16, moodTxs));
        timePatterns.add(calcTimePattern("Evening", "5 PM - 10 PM", 17, 21, moodTxs));
        timePatterns.add(calcTimePatternLateNight("Late Night", "10 PM - 6 AM", moodTxs));
        timePatterns.removeAll(Collections.singleton(null));

        // Insights list
        List<MoodInsight> insights = new ArrayList<>();
        if (highestSpendMood != null && highestSpendMood.getTotalAmount() > 0) {
            insights.add(new MoodInsight(
                    "Highest Spending Driver",
                    "You spend the most (" + formatter.format(highestSpendMood.getTotalAmount()) + ") when feeling " + highestSpendMood.getMood() + ".",
                    highestSpendMood.getEmoji()
            ));
        }

        MoodStat stressedStat = findStat(moodStats, "Stressed");
        if (stressedStat != null && overallAvg > 0 && stressedStat.getAvgAmount() > overallAvg * 1.15) {
            int pct = (int) (((stressedStat.getAvgAmount() - overallAvg) / overallAvg) * 100);
            insights.add(new MoodInsight(
                    "Stress Premium",
                    "You spend " + pct + "% more per purchase when feeling stressed compared to your baseline.",
                    "😟"
            ));
        }

        MoodStat boredStat = findStat(moodStats, "Bored");
        if (boredStat != null && boredStat.getTotalAmount() > 0) {
            insights.add(new MoodInsight(
                    "Boredom Purchases",
                    "Bored spending cost you " + formatter.format(boredStat.getTotalAmount()) + " across " + boredStat.getTransactionCount() + " transactions.",
                    "🥱"
            ));
        }

        MoodStat happyStat = findStat(moodStats, "Happy");
        if (happyStat != null && stressedStat != null && happyStat.getAvgAmount() < stressedStat.getAvgAmount()) {
            insights.add(new MoodInsight(
                    "Mindful Spending",
                    "Your happiest transactions average " + formatter.format(happyStat.getAvgAmount()) + " vs " + formatter.format(stressedStat.getAvgAmount()) + " when stressed.",
                    "😊"
            ));
        }

        // Late night
        double lateNightTotal = 0.0;
        List<TransactionEntity> lateNightTxs = new ArrayList<>();
        for (TransactionEntity tx : moodTxs) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(tx.getDateTime());
            int hr = cal.get(Calendar.HOUR_OF_DAY);
            if (hr >= 22 || hr < 6) {
                lateNightTxs.add(tx);
                lateNightTotal += tx.getAmount();
            }
        }
        if (!lateNightTxs.isEmpty()) {
            Map<String, Integer> counts = new HashMap<>();
            for (TransactionEntity tx : lateNightTxs) {
                counts.put(tx.getMood(), counts.getOrDefault(tx.getMood(), 0) + 1);
            }
            String dom = "Bored";
            int max = 0;
            for (Map.Entry<String, Integer> e : counts.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                    dom = e.getKey();
                }
            }
            insights.add(new MoodInsight(
                    "Late-Night Spending",
                    "Late night (10 PM - 6 AM) accounts for " + formatter.format(lateNightTotal) + " of spending, mostly while feeling " + dom + ".",
                    "🌙"
            ));
        }

        if (highestSpendMood != null && highestSpendMood.getTopCategory() != null && !highestSpendMood.getTopCategory().isEmpty()) {
            insights.add(new MoodInsight(
                    "Category Trigger",
                    "When feeling " + highestSpendMood.getMood() + ", your primary spending category is " + highestSpendMood.getTopCategory() + ".",
                    "🎯"
            ));
        }

        return new MoodAnalytics(
                moodTxs.size(),
                totalMoodSpend,
                moodStats,
                highestSpendMood,
                highestAvgMood,
                insights,
                timePatterns,
                true,
                threshold,
                0
        );
    }

    public static WeeklyMoodSummary generateWeeklySummary(List<TransactionEntity> transactions) {
        if (transactions == null) return null;
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        List<TransactionEntity> weeklyTxs = new ArrayList<>();
        for (TransactionEntity tx : transactions) {
            if ("EXPENSE".equals(tx.getType()) && tx.getMood() != null && !tx.getMood().trim().isEmpty() && tx.getDateTime() >= sevenDaysAgo) {
                weeklyTxs.add(tx);
            }
        }
        if (weeklyTxs.isEmpty()) return null;

        double totalSpend = 0.0;
        Map<String, Double> moodTotals = new HashMap<>();
        for (TransactionEntity tx : weeklyTxs) {
            totalSpend += tx.getAmount();
            moodTotals.put(tx.getMood(), moodTotals.getOrDefault(tx.getMood(), 0.0) + tx.getAmount());
        }

        Map.Entry<String, Double> dominant = null;
        for (Map.Entry<String, Double> entry : moodTotals.entrySet()) {
            if (dominant == null || entry.getValue() > dominant.getValue()) {
                dominant = entry;
            }
        }
        if (dominant == null) return null;

        String dominantMood = dominant.getKey();
        String dominantEmoji = getMoodEmoji(dominantMood);
        int dominantColor = getMoodColor(dominantMood);

        Set<String> positiveMoods = new HashSet<>();
        positiveMoods.add("happy");
        positiveMoods.add("celebrating");
        positiveMoods.add("neutral");

        double positiveSpend = 0.0;
        for (TransactionEntity tx : weeklyTxs) {
            if (positiveMoods.contains(tx.getMood().toLowerCase(Locale.ROOT))) {
                positiveSpend += tx.getAmount();
            }
        }

        int positivePercentage = totalSpend > 0 ? (int) ((positiveSpend / totalSpend) * 100) : 0;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        String headline = "Dominant Mood: " + dominantEmoji + " " + dominantMood;

        String summaryInsight;
        if (positivePercentage >= 70) {
            summaryInsight = "Great emotional balance! " + positivePercentage + "% of this week's spending was positive or mindful.";
        } else if ("Stressed".equalsIgnoreCase(dominantMood)) {
            summaryInsight = "Stress was your biggest spending trigger this week (" + formatter.format(dominant.getValue()) + ").";
        } else if ("Bored".equalsIgnoreCase(dominantMood)) {
            summaryInsight = "Watch out for boredom snacking/shopping (" + formatter.format(dominant.getValue()) + " spent this week).";
        } else {
            summaryInsight = formatter.format(totalSpend) + " logged across " + weeklyTxs.size() + " mood-tagged expenses this week.";
        }

        return new WeeklyMoodSummary(
                dominantMood,
                dominantEmoji,
                dominantColor,
                totalSpend,
                weeklyTxs.size(),
                positivePercentage,
                headline,
                summaryInsight
        );
    }

    private static List<TransactionEntity> filterMoodExpenses(List<TransactionEntity> transactions) {
        List<TransactionEntity> list = new ArrayList<>();
        if (transactions == null) return list;
        for (TransactionEntity tx : transactions) {
            if ("EXPENSE".equals(tx.getType()) && tx.getMood() != null && !tx.getMood().trim().isEmpty()) {
                list.add(tx);
            }
        }
        return list;
    }

    private static Map<String, List<TransactionEntity>> groupByMood(List<TransactionEntity> txs) {
        Map<String, List<TransactionEntity>> map = new HashMap<>();
        for (TransactionEntity tx : txs) {
            map.computeIfAbsent(tx.getMood(), k -> new ArrayList<>()).add(tx);
        }
        return map;
    }

    private static String extractCategory(String note) {
        if (note == null || note.trim().isEmpty()) return "Other Expense";
        int colonIdx = note.indexOf(':');
        if (colonIdx > 0) {
            String cat = note.substring(0, colonIdx).trim();
            if (!cat.isEmpty()) return cat;
        }
        return note.trim();
    }

    private static MoodStat findStat(List<MoodStat> stats, String mood) {
        for (MoodStat s : stats) {
            if (s.getMood().equalsIgnoreCase(mood)) return s;
        }
        return null;
    }

    private static TimeOfDayMoodPattern calcTimePattern(String name, String range, int minHour, int maxHour, List<TransactionEntity> txs) {
        List<TransactionEntity> slotTxs = new ArrayList<>();
        double total = 0.0;
        for (TransactionEntity tx : txs) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(tx.getDateTime());
            int hr = cal.get(Calendar.HOUR_OF_DAY);
            if (hr >= minHour && hr <= maxHour) {
                slotTxs.add(tx);
                total += tx.getAmount();
            }
        }
        if (slotTxs.isEmpty()) return null;

        Map<String, Double> moodTotals = new HashMap<>();
        for (TransactionEntity tx : slotTxs) {
            moodTotals.put(tx.getMood(), moodTotals.getOrDefault(tx.getMood(), 0.0) + tx.getAmount());
        }
        String dom = "Neutral";
        double maxAmt = 0.0;
        for (Map.Entry<String, Double> e : moodTotals.entrySet()) {
            if (e.getValue() > maxAmt) {
                maxAmt = e.getValue();
                dom = e.getKey();
            }
        }
        return new TimeOfDayMoodPattern(name, range, dom, getMoodEmoji(dom), total, getMoodColor(dom));
    }

    private static TimeOfDayMoodPattern calcTimePatternLateNight(String name, String range, List<TransactionEntity> txs) {
        List<TransactionEntity> slotTxs = new ArrayList<>();
        double total = 0.0;
        for (TransactionEntity tx : txs) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(tx.getDateTime());
            int hr = cal.get(Calendar.HOUR_OF_DAY);
            if (hr >= 22 || hr < 6) {
                slotTxs.add(tx);
                total += tx.getAmount();
            }
        }
        if (slotTxs.isEmpty()) return null;

        Map<String, Double> moodTotals = new HashMap<>();
        for (TransactionEntity tx : slotTxs) {
            moodTotals.put(tx.getMood(), moodTotals.getOrDefault(tx.getMood(), 0.0) + tx.getAmount());
        }
        String dom = "Neutral";
        double maxAmt = 0.0;
        for (Map.Entry<String, Double> e : moodTotals.entrySet()) {
            if (e.getValue() > maxAmt) {
                maxAmt = e.getValue();
                dom = e.getKey();
            }
        }
        return new TimeOfDayMoodPattern(name, range, dom, getMoodEmoji(dom), total, getMoodColor(dom));
    }
}
