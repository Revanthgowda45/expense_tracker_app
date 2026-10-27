package com.example.studentexpensetracker.domain.split;

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SplitCalculatorTest {

    @Test
    public void splitEqually_divides100Among3PeopleWithExactPennyRounding() {
        List<ExpenseShareEntity> shares = SplitCalculator.splitEqually(100.0, Arrays.asList(1L, 2L, 3L));
        assertEquals(3, shares.size());

        // 100 / 3 = 33.33 with 0.01 remainder -> 33.34, 33.33, 33.33
        assertEquals(33.34, shares.get(0).getOwedAmount(), 0.001);
        assertEquals(33.33, shares.get(1).getOwedAmount(), 0.001);
        assertEquals(33.33, shares.get(2).getOwedAmount(), 0.001);

        double total = 0.0;
        for (ExpenseShareEntity s : shares) {
            total += s.getOwedAmount();
        }
        total = Math.round(total * 100.0) / 100.0;
        assertEquals(100.0, total, 0.001);
    }

    @Test
    public void splitByExactAmounts_succeedsWhenSumMatchesTotal() {
        Map<Long, Double> amounts = new HashMap<>();
        amounts.put(1L, 40.0);
        amounts.put(2L, 35.50);
        amounts.put(3L, 24.50);

        SplitCalculator.Result<List<ExpenseShareEntity>> result = SplitCalculator.splitByExactAmounts(100.0, amounts);

        assertTrue(result.isSuccess());
        List<ExpenseShareEntity> shares = result.getOrThrow();
        assertEquals(40.0, getShareForFriend(shares, 1L), 0.001);
        assertEquals(35.50, getShareForFriend(shares, 2L), 0.001);
        assertEquals(24.50, getShareForFriend(shares, 3L), 0.001);
    }

    @Test
    public void splitByExactAmounts_failsWhenSumDoesNotMatchTotal() {
        Map<Long, Double> amounts = new HashMap<>();
        amounts.put(1L, 40.0);
        amounts.put(2L, 30.0);

        SplitCalculator.Result<List<ExpenseShareEntity>> result = SplitCalculator.splitByExactAmounts(100.0, amounts);

        assertTrue(result.isFailure());
    }

    @Test
    public void splitByPercentages_divides250WithPercentages() {
        Map<Long, Double> percentages = new HashMap<>();
        percentages.put(1L, 50.0);
        percentages.put(2L, 30.0);
        percentages.put(3L, 20.0);

        SplitCalculator.Result<List<ExpenseShareEntity>> result = SplitCalculator.splitByPercentages(250.0, percentages);

        assertTrue(result.isSuccess());
        List<ExpenseShareEntity> shares = result.getOrThrow();
        assertEquals(125.0, getShareForFriend(shares, 1L), 0.001);
        assertEquals(75.0, getShareForFriend(shares, 2L), 0.001);
        assertEquals(50.0, getShareForFriend(shares, 3L), 0.001);

        double total = 0.0;
        for (ExpenseShareEntity s : shares) {
            total += s.getOwedAmount();
        }
        assertEquals(250.0, total, 0.001);
    }

    @Test
    public void splitByShares_handlesRatioOn100Total() {
        Map<Long, Double> ratios = new HashMap<>();
        ratios.put(1L, 2.0);
        ratios.put(2L, 1.0);
        ratios.put(3L, 1.0);

        SplitCalculator.Result<List<ExpenseShareEntity>> result = SplitCalculator.splitByShares(100.0, ratios);

        assertTrue(result.isSuccess());
        List<ExpenseShareEntity> shares = result.getOrThrow();
        assertEquals(50.0, getShareForFriend(shares, 1L), 0.001);
        assertEquals(25.0, getShareForFriend(shares, 2L), 0.001);
        assertEquals(25.0, getShareForFriend(shares, 3L), 0.001);

        double total = 0.0;
        for (ExpenseShareEntity s : shares) {
            total += s.getOwedAmount();
        }
        assertEquals(100.0, total, 0.001);
    }

    private double getShareForFriend(List<ExpenseShareEntity> shares, long friendId) {
        for (ExpenseShareEntity s : shares) {
            if (s.getFriendId() == friendId) {
                return s.getOwedAmount();
            }
        }
        return -1.0;
    }
}
