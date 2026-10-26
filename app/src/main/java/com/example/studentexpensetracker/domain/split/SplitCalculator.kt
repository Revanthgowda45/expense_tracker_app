package com.example.studentexpensetracker.domain.split

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

enum class SplitType {
    EQUALLY,
    EXACT_AMOUNTS,
    PERCENTAGES,
    SHARES
}

object SplitCalculator {

    /**
     * Splits totalAmount equally among selected members.
     * Accurately distributes rounding remainder so sum(shares) == totalAmount down to 2 decimal places.
     */
    fun splitEqually(
        totalAmount: Double,
        memberIds: List<Long>,
        sharedExpenseId: Long = 0L
    ): List<ExpenseShareEntity> {
        if (memberIds.isEmpty() || totalAmount <= 0) return emptyList()

        val roundedTotal = roundToCents(totalAmount)
        val count = memberIds.size
        val baseShare = floor((roundedTotal / count) * 100.0) / 100.0
        var remainder = roundToCents(roundedTotal - (baseShare * count))

        return memberIds.map { memberId ->
            val extra = if (remainder > 0.009) {
                remainder = roundToCents(remainder - 0.01)
                0.01
            } else {
                0.0
            }
            ExpenseShareEntity(
                sharedExpenseId = sharedExpenseId,
                friendId = memberId,
                owedAmount = roundToCents(baseShare + extra)
            )
        }
    }

    /**
     * Splits by exact amounts specified per member.
     * Validates that all amounts are non-negative and sum equals totalAmount.
     */
    fun splitByExactAmounts(
        totalAmount: Double,
        amounts: Map<Long, Double>,
        sharedExpenseId: Long = 0L
    ): Result<List<ExpenseShareEntity>> {
        val roundedTotal = roundToCents(totalAmount)
        if (amounts.values.any { it < 0 }) {
            return Result.failure(IllegalArgumentException("Amounts cannot be negative"))
        }

        val sum = roundToCents(amounts.values.sum())
        if (abs(sum - roundedTotal) > 0.01) {
            return Result.failure(
                IllegalArgumentException("Entered amounts sum to ₹$sum, which does not match total of ₹$roundedTotal")
            )
        }

        val shares = amounts.map { (memberId, amount) ->
            ExpenseShareEntity(
                sharedExpenseId = sharedExpenseId,
                friendId = memberId,
                owedAmount = roundToCents(amount)
            )
        }
        return Result.success(shares)
    }

    /**
     * Splits by percentages specified per member.
     * Validates that percentages sum to 100%. Handles rounding remainder on the largest percentage share.
     */
    fun splitByPercentages(
        totalAmount: Double,
        percentages: Map<Long, Double>,
        sharedExpenseId: Long = 0L
    ): Result<List<ExpenseShareEntity>> {
        val roundedTotal = roundToCents(totalAmount)
        if (percentages.values.any { it < 0 }) {
            return Result.failure(IllegalArgumentException("Percentages cannot be negative"))
        }

        val totalPercentage = round(percentages.values.sum() * 100.0) / 100.0
        if (abs(totalPercentage - 100.0) > 0.05) {
            return Result.failure(
                IllegalArgumentException("Percentages sum to $totalPercentage%, must equal 100%")
            )
        }

        val rawShares = percentages.mapValues { (_, percent) ->
            roundToCents(roundedTotal * (percent / 100.0))
        }

        val currentSum = roundToCents(rawShares.values.sum())
        val diff = roundToCents(roundedTotal - currentSum)

        // Find member with highest percentage to absorb any rounding diff
        val highestMember = percentages.maxByOrNull { it.value }?.key

        val shares = percentages.keys.map { memberId ->
            var shareAmount = rawShares[memberId] ?: 0.0
            if (memberId == highestMember) {
                shareAmount = roundToCents(shareAmount + diff)
            }
            ExpenseShareEntity(
                sharedExpenseId = sharedExpenseId,
                friendId = memberId,
                owedAmount = shareAmount
            )
        }

        return Result.success(shares)
    }

    /**
     * Splits by weight/shares ratio (e.g. 2:1:1).
     * Distributes total proportionally and handles rounding difference on the largest share.
     */
    fun splitByShares(
        totalAmount: Double,
        sharesRatio: Map<Long, Double>,
        sharedExpenseId: Long = 0L
    ): Result<List<ExpenseShareEntity>> {
        val roundedTotal = roundToCents(totalAmount)
        if (sharesRatio.values.any { it < 0 }) {
            return Result.failure(IllegalArgumentException("Shares cannot be negative"))
        }

        val totalShares = sharesRatio.values.sum()
        if (totalShares <= 0.001) {
            return Result.failure(IllegalArgumentException("Total shares must be greater than 0"))
        }

        val rawAmounts = sharesRatio.mapValues { (_, ratio) ->
            roundToCents((ratio / totalShares) * roundedTotal)
        }

        val currentSum = roundToCents(rawAmounts.values.sum())
        val diff = roundToCents(roundedTotal - currentSum)

        val highestMember = sharesRatio.maxByOrNull { it.value }?.key

        val shares = sharesRatio.keys.map { memberId ->
            var shareAmount = rawAmounts[memberId] ?: 0.0
            if (memberId == highestMember) {
                shareAmount = roundToCents(shareAmount + diff)
            }
            ExpenseShareEntity(
                sharedExpenseId = sharedExpenseId,
                friendId = memberId,
                owedAmount = shareAmount
            )
        }

        return Result.success(shares)
    }

    private fun roundToCents(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
}
