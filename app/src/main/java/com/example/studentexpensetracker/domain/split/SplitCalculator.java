package com.example.studentexpensetracker.domain.split;

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitCalculator {

    public static class Result<T> {
        private final T value;
        private final String error;
        private final boolean success;

        private Result(T value, String error, boolean success) {
            this.value = value;
            this.error = error;
            this.success = success;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(value, null, true);
        }

        public static <T> Result<T> failure(String error) {
            return new Result<>(null, error, false);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isFailure() {
            return !success;
        }

        public T getValue() {
            return value;
        }

        public T getOrThrow() {
            if (!success) {
                throw new IllegalArgumentException(error);
            }
            return value;
        }

        public String getError() {
            return error;
        }
    }

    public static List<ExpenseShareEntity> splitEqually(
            double totalAmount,
            List<Long> memberIds,
            long sharedExpenseId) {
        if (memberIds == null || memberIds.isEmpty() || totalAmount <= 0) {
            return Collections.emptyList();
        }

        double roundedTotal = roundToCents(totalAmount);
        int count = memberIds.size();
        double baseShare = Math.floor((roundedTotal / count) * 100.0) / 100.0;
        double remainder = roundToCents(roundedTotal - (baseShare * count));

        List<ExpenseShareEntity> shares = new ArrayList<>();
        for (Long memberId : memberIds) {
            double extra = 0.0;
            if (remainder > 0.009) {
                remainder = roundToCents(remainder - 0.01);
                extra = 0.01;
            }
            shares.add(new ExpenseShareEntity(
                    sharedExpenseId,
                    memberId,
                    roundToCents(baseShare + extra)
            ));
        }
        return shares;
    }

    public static List<ExpenseShareEntity> splitEqually(double totalAmount, List<Long> memberIds) {
        return splitEqually(totalAmount, memberIds, 0L);
    }

    public static Result<List<ExpenseShareEntity>> splitByExactAmounts(
            double totalAmount,
            Map<Long, Double> amounts,
            long sharedExpenseId) {
        double roundedTotal = roundToCents(totalAmount);
        for (Double amt : amounts.values()) {
            if (amt < 0) {
                return Result.failure("Amounts cannot be negative");
            }
        }

        double sum = 0.0;
        for (Double amt : amounts.values()) {
            sum += amt;
        }
        sum = roundToCents(sum);

        if (Math.abs(sum - roundedTotal) > 0.01) {
            return Result.failure("Entered amounts sum to ₹" + sum + ", which does not match total of ₹" + roundedTotal);
        }

        List<ExpenseShareEntity> shares = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : amounts.entrySet()) {
            shares.add(new ExpenseShareEntity(
                    sharedExpenseId,
                    entry.getKey(),
                    roundToCents(entry.getValue())
            ));
        }
        return Result.success(shares);
    }

    public static Result<List<ExpenseShareEntity>> splitByExactAmounts(double totalAmount, Map<Long, Double> amounts) {
        return splitByExactAmounts(totalAmount, amounts, 0L);
    }

    public static Result<List<ExpenseShareEntity>> splitByPercentages(
            double totalAmount,
            Map<Long, Double> percentages,
            long sharedExpenseId) {
        double roundedTotal = roundToCents(totalAmount);
        for (Double pct : percentages.values()) {
            if (pct < 0) {
                return Result.failure("Percentages cannot be negative");
            }
        }

        double totalPct = 0.0;
        for (Double pct : percentages.values()) {
            totalPct += pct;
        }
        totalPct = Math.round(totalPct * 100.0) / 100.0;

        if (Math.abs(totalPct - 100.0) > 0.05) {
            return Result.failure("Percentages sum to " + totalPct + "%, must equal 100%");
        }

        Map<Long, Double> rawShares = new HashMap<>();
        double currentSum = 0.0;
        Long highestMember = null;
        double maxPct = -1.0;

        for (Map.Entry<Long, Double> entry : percentages.entrySet()) {
            double share = roundToCents(roundedTotal * (entry.getValue() / 100.0));
            rawShares.put(entry.getKey(), share);
            currentSum += share;

            if (entry.getValue() > maxPct) {
                maxPct = entry.getValue();
                highestMember = entry.getKey();
            }
        }

        currentSum = roundToCents(currentSum);
        double diff = roundToCents(roundedTotal - currentSum);

        List<ExpenseShareEntity> shares = new ArrayList<>();
        for (Long memberId : percentages.keySet()) {
            double shareAmount = rawShares.getOrDefault(memberId, 0.0);
            if (memberId.equals(highestMember)) {
                shareAmount = roundToCents(shareAmount + diff);
            }
            shares.add(new ExpenseShareEntity(sharedExpenseId, memberId, shareAmount));
        }

        return Result.success(shares);
    }

    public static Result<List<ExpenseShareEntity>> splitByPercentages(double totalAmount, Map<Long, Double> percentages) {
        return splitByPercentages(totalAmount, percentages, 0L);
    }

    public static Result<List<ExpenseShareEntity>> splitByShares(
            double totalAmount,
            Map<Long, Double> sharesRatio,
            long sharedExpenseId) {
        double roundedTotal = roundToCents(totalAmount);
        for (Double ratio : sharesRatio.values()) {
            if (ratio < 0) {
                return Result.failure("Shares cannot be negative");
            }
        }

        double totalShares = 0.0;
        for (Double ratio : sharesRatio.values()) {
            totalShares += ratio;
        }

        if (totalShares <= 0.001) {
            return Result.failure("Total shares must be greater than 0");
        }

        Map<Long, Double> rawAmounts = new HashMap<>();
        double currentSum = 0.0;
        Long highestMember = null;
        double maxRatio = -1.0;

        for (Map.Entry<Long, Double> entry : sharesRatio.entrySet()) {
            double amt = roundToCents((entry.getValue() / totalShares) * roundedTotal);
            rawAmounts.put(entry.getKey(), amt);
            currentSum += amt;

            if (entry.getValue() > maxRatio) {
                maxRatio = entry.getValue();
                highestMember = entry.getKey();
            }
        }

        currentSum = roundToCents(currentSum);
        double diff = roundToCents(roundedTotal - currentSum);

        List<ExpenseShareEntity> shares = new ArrayList<>();
        for (Long memberId : sharesRatio.keySet()) {
            double shareAmount = rawAmounts.getOrDefault(memberId, 0.0);
            if (memberId.equals(highestMember)) {
                shareAmount = roundToCents(shareAmount + diff);
            }
            shares.add(new ExpenseShareEntity(sharedExpenseId, memberId, shareAmount));
        }

        return Result.success(shares);
    }

    public static Result<List<ExpenseShareEntity>> splitByShares(double totalAmount, Map<Long, Double> sharesRatio) {
        return splitByShares(totalAmount, sharesRatio, 0L);
    }

    private static double roundToCents(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
