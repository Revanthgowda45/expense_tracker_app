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

    @Test
    fun `simplifyDebts simplifies transitive debt where A owes B and B owes C so A pays C directly`() {
        // A owes B 200, B owes C 200
        // Net balance: A = -200, B = 0, C = +200
        // Expected simplified debt: A owes C 200 (1 transaction instead of 2)
        val expenses = listOf(
            SharedExpenseEntity(1, 1, "B paid for A", 200.0, paidByFriendId = 2L, dateTime = 0L),
            SharedExpenseEntity(2, 1, "C paid for B", 200.0, paidByFriendId = 3L, dateTime = 0L)
        )
        val shares = listOf(
            ExpenseShareEntity(1, 1L, 200.0), // A owes 200
            ExpenseShareEntity(2, 2L, 200.0)  // B owes 200
        )

        val debts = DebtSimplifier.simplifyDebts(expenses, shares, emptyList())

        assertEquals(1, debts.size)
        assertEquals(1L, debts[0].fromId) // A
        assertEquals(3L, debts[0].toId)   // C
        assertEquals(200.0, debts[0].amount, 0.01)
    }

    @Test
    fun `simplifyDebts handles complex 4-person Goa Trip scenario`() {
        // Goa Trip: 4 friends (1: Rahul, 2: Priya, 3: Aman, 4: Rohit)
        // Expense 1: Priya (2) paid Hotel 4000 (split equally: 1000 each)
        // Expense 2: Rahul (1) paid Dinner 1200 for Rahul (1) and Aman (3) (600 each)
        val expenses = listOf(
            SharedExpenseEntity(1, 1, "Hotel", 4000.0, paidByFriendId = 2L, dateTime = 0L),
            SharedExpenseEntity(2, 1, "Dinner", 1200.0, paidByFriendId = 1L, dateTime = 0L)
        )
        val shares = listOf(
            // Hotel: 1000 each
            ExpenseShareEntity(1, 1L, 1000.0),
            ExpenseShareEntity(1, 2L, 1000.0),
            ExpenseShareEntity(1, 3L, 1000.0),
            ExpenseShareEntity(1, 4L, 1000.0),
            // Dinner: 600 each for 1 and 3
            ExpenseShareEntity(2, 1L, 600.0),
            ExpenseShareEntity(2, 3L, 600.0)
        )

        // Balances:
        // Priya (2): +4000 - 1000 = +3000 (creditor)
        // Rahul (1): +1200 - 1000 - 600 = -400 (debtor owes 400)
        // Aman (3): -1000 - 600 = -1600 (debtor owes 1600)
        // Rohit (4): -1000 = -1000 (debtor owes 1000)
        // Sum of debts: 400 + 1600 + 1000 = 3000 = Priya's credit!

        val debts = DebtSimplifier.simplifyDebts(expenses, shares, emptyList())

        // Minimum transactions: exactly 3 payments directly to Priya
        assertEquals(3, debts.size)
        assertTrue(debts.all { it.toId == 2L }) // All pay Priya
        assertEquals(3000.0, debts.sumOf { it.amount }, 0.01)
    }
}
