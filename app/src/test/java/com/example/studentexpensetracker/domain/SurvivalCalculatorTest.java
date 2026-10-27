package com.example.studentexpensetracker.domain;

import org.junit.Test;
import java.util.Calendar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SurvivalCalculatorTest {

    @Test
    public void calculate_returnsSafe_whenDaysLeftIsWellAboveDaysUntilIncome() {
        // Setup: Current balance = 2000, daily spend = 100. Days left = 20.
        // If income is in 10 days, we are SAFE (20 > 10 + 3)
        SurvivalStats stats = SurvivalCalculator.calculate(
                2000.0,
                1400.0,
                14,
                15,
                50.0,
                getMillisForDayOfMonth(5)
        );

        assertEquals(SurvivalStatus.SAFE, stats.getStatus());
        assertEquals(20, stats.getDaysLeft());
        assertEquals(10, stats.getDaysUntilNextIncome());
        assertEquals(200.0, stats.getSafeDailyLimit(), 0.01);
        assertEquals(150.0, stats.getTodayRemaining(), 0.01);
    }

    @Test
    public void calculate_returnsTight_whenDaysLeftIsExactlyDaysUntilIncome() {
        SurvivalStats stats = SurvivalCalculator.calculate(
                1000.0,
                1400.0,
                14,
                15,
                20.0,
                getMillisForDayOfMonth(5)
        );

        assertEquals(SurvivalStatus.TIGHT, stats.getStatus());
        assertEquals(10, stats.getDaysLeft());
        assertEquals(80.0, stats.getTodayRemaining(), 0.01);
    }

    @Test
    public void calculate_returnsDanger_whenDaysLeftIsLessThanDaysUntilIncome() {
        SurvivalStats stats = SurvivalCalculator.calculate(
                500.0,
                1400.0,
                14,
                15,
                0.0,
                getMillisForDayOfMonth(5)
        );

        assertEquals(SurvivalStatus.DANGER, stats.getStatus());
        assertEquals(5, stats.getDaysLeft());
    }

    @Test
    public void calculate_handlesZeroRecentExpensesGracefully() {
        SurvivalStats stats = SurvivalCalculator.calculate(
                1000.0,
                0.0,
                14,
                15,
                0.0,
                getMillisForDayOfMonth(5)
        );

        assertEquals(SurvivalStatus.SAFE, stats.getStatus());
        assertEquals(999, stats.getDaysLeft());
        assertEquals(100.0, stats.getSafeDailyLimit(), 0.01);
        assertEquals(100.0, stats.getTodayRemaining(), 0.01);
    }

    @Test
    public void calculate_handlesEdgeCaseWhenAllowanceDayIsToday() {
        long millis = getMillisForDayOfMonth(15);
        SurvivalStats stats = SurvivalCalculator.calculate(
                100.0,
                1400.0,
                14,
                15,
                0.0,
                millis
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        assertEquals(maxDays, stats.getDaysUntilNextIncome());
    }

    @Test
    public void calculate_returnsDangerWithZeroDaysLeftWhenCurrentBalanceIsZeroOrNegative() {
        SurvivalStats stats = SurvivalCalculator.calculate(
                0.0,
                1400.0,
                14,
                15,
                50.0,
                getMillisForDayOfMonth(5)
        );

        assertEquals(SurvivalStatus.DANGER, stats.getStatus());
        assertEquals(0, stats.getDaysLeft());
        assertEquals(0.0, stats.getSafeDailyLimit(), 0.001);
        assertEquals(0.0, stats.getTodayRemaining(), 0.001);
    }

    @Test
    public void calculate_handlesNullOrInvalidAllowanceDayCorrectly() {
        SurvivalStats stats = SurvivalCalculator.calculate(
                3000.0,
                1400.0,
                14,
                null,
                25.0,
                System.currentTimeMillis()
        );

        assertFalse(stats.isIncomeDateSet());
        assertEquals(30, stats.getDaysLeft());
        assertEquals(100.0, stats.getSafeDailyLimit(), 0.01);
        assertEquals(75.0, stats.getTodayRemaining(), 0.01);
        assertEquals(SurvivalStatus.SAFE, stats.getStatus());
    }

    @Test
    public void calculate_clampsTodayRemainingToZeroIfTodaySpentExceedsSafeDailyLimit() {
        SurvivalStats stats = SurvivalCalculator.calculate(
                1000.0,
                1400.0,
                14,
                15,
                250.0,
                getMillisForDayOfMonth(5)
        );

        assertEquals(100.0, stats.getSafeDailyLimit(), 0.01);
        assertEquals(0.0, stats.getTodayRemaining(), 0.001);
    }

    private long getMillisForDayOfMonth(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar.getTimeInMillis();
    }
}
