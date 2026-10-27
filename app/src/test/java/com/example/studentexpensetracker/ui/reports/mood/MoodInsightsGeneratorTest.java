package com.example.studentexpensetracker.ui.reports.mood;

import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MoodInsightsGeneratorTest {

    @Test
    public void generate_returnsEmptyList_whenNotEnoughData() {
        List<TransactionEntity> transactions = Arrays.asList(
                createTx(100.0, "Happy"),
                createTx(50.0, "Sad")
        );
        List<MoodInsight> insights = MoodInsightsGenerator.generate(transactions);
        assertTrue(insights.isEmpty());
    }

    @Test
    public void generate_identifiesHighestSpendingMood() {
        List<TransactionEntity> transactions = Arrays.asList(
                createTx(100.0, "Happy"),
                createTx(50.0, "Happy"),
                createTx(500.0, "Sad"),
                createTx(100.0, "Sad"),
                createTx(50.0, "Bored")
        );
        List<MoodInsight> insights = MoodInsightsGenerator.generate(transactions);

        assertTrue(insights.size() > 0);

        MoodInsight highestInsight = null;
        for (MoodInsight insight : insights) {
            if ("Highest Spending Mood".equals(insight.getTitle())) {
                highestInsight = insight;
                break;
            }
        }
        assertNotNull(highestInsight);
        assertTrue(highestInsight.getDescription().contains("Sad"));
    }

    @Test
    public void generate_identifiesStressSpendingCategory() {
        List<TransactionEntity> transactions = Arrays.asList(
                createTx(100.0, "Stressed", "Food: Pizza", 0L),
                createTx(50.0, "Stressed", "Food: Burger", 0L),
                createTx(500.0, "Stressed", "Transport: Cab", 0L),
                createTx(100.0, "Happy", "Food", 0L),
                createTx(50.0, "Bored", "Entertainment", 0L)
        );

        List<MoodInsight> insights = MoodInsightsGenerator.generate(transactions);

        MoodInsight stressInsight = null;
        for (MoodInsight insight : insights) {
            if ("Stress Spending".equals(insight.getTitle())) {
                stressInsight = insight;
                break;
            }
        }
        assertNotNull(stressInsight);
        assertTrue(stressInsight.getDescription().contains("Food"));
    }

    @Test
    public void analyze_returnsThresholdNotReached_whenFewerThan5Transactions() {
        List<TransactionEntity> transactions = Arrays.asList(
                createTx(100.0, "Happy"),
                createTx(50.0, "Sad")
        );
        MoodAnalytics analytics = MoodInsightsGenerator.analyze(transactions);
        assertFalse(analytics.isThresholdReached());
        assertEquals(2, analytics.getTotalMoodTransactions());
        assertEquals(3, analytics.getRemainingToThreshold());
    }

    @Test
    public void analyze_computesCorrectBreakdownAndTopSpendingMood() {
        List<TransactionEntity> transactions = Arrays.asList(
                createTx(200.0, "Happy", "Food: Lunch", 0L),
                createTx(300.0, "Happy", "Food: Dinner", 0L),
                createTx(1000.0, "Stressed", "Shopping: Clothes", 0L),
                createTx(500.0, "Stressed", "Food: Snacks", 0L),
                createTx(100.0, "Bored", "Entertainment: Movie", 0L)
        );
        MoodAnalytics analytics = MoodInsightsGenerator.analyze(transactions);
        assertTrue(analytics.isThresholdReached());
        assertEquals(5, analytics.getTotalMoodTransactions());
        assertEquals(2100.0, analytics.getTotalMoodSpend(), 0.01);
        assertNotNull(analytics.getHighestSpendMood());
        assertEquals("Stressed", analytics.getHighestSpendMood().getMood());
        assertEquals(1500.0, analytics.getHighestSpendMood().getTotalAmount(), 0.01);
        assertFalse(analytics.getInsights().isEmpty());
    }

    @Test
    public void generateWeeklySummary_calculatesDominantMoodAndPositiveRatio() {
        long now = System.currentTimeMillis();
        List<TransactionEntity> transactions = Arrays.asList(
                createTx(100.0, "Happy", "Note", now),
                createTx(200.0, "Celebrating", "Note", now),
                createTx(100.0, "Stressed", "Note", now)
        );
        WeeklyMoodSummary weeklySummary = MoodInsightsGenerator.generateWeeklySummary(transactions);
        assertNotNull(weeklySummary);
        assertEquals(3, weeklySummary.getMoodTransactionCount());
        assertEquals(400.0, weeklySummary.getTotalWeeklySpend(), 0.01);
        assertEquals(75, weeklySummary.getPositivePercentage());
    }

    private TransactionEntity createTx(double amount, String mood) {
        return createTx(amount, mood, "Note", 0L);
    }

    private TransactionEntity createTx(double amount, String mood, String note, long dateTime) {
        return new TransactionEntity(
                amount,
                "EXPENSE",
                1,
                note,
                "CASH",
                dateTime > 0 ? dateTime : System.currentTimeMillis(),
                false,
                null,
                0L,
                mood,
                null
        );
    }
}
