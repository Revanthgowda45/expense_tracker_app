package com.example.studentexpensetracker.domain.split

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class DebtSimplifierTest {

    @Test
    fun `simplifyDebts handles simple split between two people`() {
        // A pays 100 for A and B. B should owe A 50.
        val expenses = listOf(SharedExpenseEntity(1, 1, "Food", 100.0, 1L, 0L))
        val shares = listOf(
            ExpenseShareEntity(1, 1L, 50.0),
            ExpenseShareEntity(1, 2L, 50.0)
        )

        val debts = DebtSimplifier.simplifyDebts(expenses, shares, emptyList())

        assertEquals(1, debts.size)
        assertEquals(2L, debts[0].fromId)
        assertEquals(1L, debts[0].toId)
        assertEquals(50.0, debts[0].amount, 0.01)
    }

    @Test
    fun `simplifyDebts simplifies circular debt`() {
        // A pays 100 for B
        // B pays 100 for C
        // C pays 100 for A
        // Result should be 0 debts (they cancel out)
        
        val expenses = listOf(
            SharedExpenseEntity(1, 1, "E1", 100.0, 1L, 0L),
            SharedExpenseEntity(2, 1, "E2", 100.0, 2L, 0L),
            SharedExpenseEntity(3, 1, "E3", 100.0, 3L, 0L)
        )
        val shares = listOf(
            ExpenseShareEntity(1, 2L, 100.0), // B owes A 100
            ExpenseShareEntity(2, 3L, 100.0), // C owes B 100
            ExpenseShareEntity(3, 1L, 100.0)  // A owes C 100
        )

        val debts = DebtSimplifier.simplifyDebts(expenses, shares, emptyList())

        assertTrue(debts.isEmpty())
    }

    @Test
    fun `simplifyDebts handles partial settlement`() {
        // A pays 100 for A and B. (B owes A 50)
        // B settles 20.
        // Result: B owes A 30.
        val expenses = listOf(SharedExpenseEntity(1, 1, "Food", 100.0, 1L, 0L))
        val shares = listOf(
            ExpenseShareEntity(1, 1L, 50.0),
            ExpenseShareEntity(1, 2L, 50.0)
        )
        val settlements = listOf(SettlementEntity(1, 1, 2L, 1L, 20.0, 0L))

        val debts = DebtSimplifier.simplifyDebts(expenses, shares, settlements)

        assertEquals(1, debts.size)
        assertEquals(2L, debts[0].fromId)
        assertEquals(1L, debts[0].toId)
        assertEquals(30.0, debts[0].amount, 0.01)
    }
}
