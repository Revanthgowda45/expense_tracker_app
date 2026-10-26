package com.example.studentexpensetracker.domain.split

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.round

class SplitCalculatorTest {

    @Test
    fun `splitEqually divides 100 among 3 people with exact penny rounding to 100`() {
        val shares = SplitCalculator.splitEqually(100.0, listOf(1L, 2L, 3L))
        assertEquals(3, shares.size)

        // 100 / 3 = 33.33 with 0.01 remainder -> 33.34, 33.33, 33.33
        assertEquals(33.34, shares[0].owedAmount, 0.001)
        assertEquals(33.33, shares[1].owedAmount, 0.001)
        assertEquals(33.33, shares[2].owedAmount, 0.001)

        val total = round(shares.sumOf { it.owedAmount } * 100.0) / 100.0
        assertEquals(100.0, total, 0.001)
    }

    @Test
    fun `splitByExactAmounts succeeds when sum matches total`() {
        val amounts = mapOf(1L to 40.0, 2L to 35.50, 3L to 24.50)
        val result = SplitCalculator.splitByExactAmounts(100.0, amounts)

        assertTrue(result.isSuccess)
        val shares = result.getOrThrow()
        assertEquals(40.0, shares.first { it.friendId == 1L }.owedAmount, 0.001)
        assertEquals(35.50, shares.first { it.friendId == 2L }.owedAmount, 0.001)
        assertEquals(24.50, shares.first { it.friendId == 3L }.owedAmount, 0.001)
    }

    @Test
    fun `splitByExactAmounts fails when sum does not match total`() {
        val amounts = mapOf(1L to 40.0, 2L to 30.0)
        val result = SplitCalculator.splitByExactAmounts(100.0, amounts)

        assertTrue(result.isFailure)
    }

    @Test
    fun `splitByPercentages divides 250 with 50 percent, 30 percent, 20 percent`() {
        val percentages = mapOf(1L to 50.0, 2L to 30.0, 3L to 20.0)
        val result = SplitCalculator.splitByPercentages(250.0, percentages)

        assertTrue(result.isSuccess)
        val shares = result.getOrThrow()
        assertEquals(125.0, shares.first { it.friendId == 1L }.owedAmount, 0.001)
        assertEquals(75.0, shares.first { it.friendId == 2L }.owedAmount, 0.001)
        assertEquals(50.0, shares.first { it.friendId == 3L }.owedAmount, 0.001)
        assertEquals(250.0, shares.sumOf { it.owedAmount }, 0.001)
    }

    @Test
    fun `splitByShares handles 2 to 1 to 1 ratio on 100 total`() {
        val ratios = mapOf(1L to 2.0, 2L to 1.0, 3L to 1.0)
        val result = SplitCalculator.splitByShares(100.0, ratios)

        assertTrue(result.isSuccess)
        val shares = result.getOrThrow()
        assertEquals(50.0, shares.first { it.friendId == 1L }.owedAmount, 0.001)
        assertEquals(25.0, shares.first { it.friendId == 2L }.owedAmount, 0.001)
        assertEquals(25.0, shares.first { it.friendId == 3L }.owedAmount, 0.001)
        assertEquals(100.0, shares.sumOf { it.owedAmount }, 0.001)
    }
}
