package com.example.studentexpensetracker.domain.split;

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DebtSimplifierTest {

    @Test
    public void simplifyDebts_handlesSimpleSplitBetweenTwoPeople() {
        // A pays 100 for A and B. B should owe A 50.
        List<SharedExpenseEntity> expenses = Collections.singletonList(
                new SharedExpenseEntity(1, 1, "Food", 100.0, 1L, 0L)
        );
        List<ExpenseShareEntity> shares = Arrays.asList(
                new ExpenseShareEntity(1, 1L, 50.0),
                new ExpenseShareEntity(1, 2L, 50.0)
        );

        List<Debt> debts = DebtSimplifier.simplifyDebts(expenses, shares, Collections.emptyList());

        assertEquals(1, debts.size());
        assertEquals(2L, debts.get(0).getFromId());
        assertEquals(1L, debts.get(0).getToId());
        assertEquals(50.0, debts.get(0).getAmount(), 0.01);
    }

    @Test
    public void simplifyDebts_simplifiesCircularDebt() {
        List<SharedExpenseEntity> expenses = Arrays.asList(
                new SharedExpenseEntity(1, 1, "E1", 100.0, 1L, 0L),
                new SharedExpenseEntity(2, 1, "E2", 100.0, 2L, 0L),
                new SharedExpenseEntity(3, 1, "E3", 100.0, 3L, 0L)
        );
        List<ExpenseShareEntity> shares = Arrays.asList(
                new ExpenseShareEntity(1, 2L, 100.0), // B owes A 100
                new ExpenseShareEntity(2, 3L, 100.0), // C owes B 100
                new ExpenseShareEntity(3, 1L, 100.0)  // A owes C 100
        );

        List<Debt> debts = DebtSimplifier.simplifyDebts(expenses, shares, Collections.emptyList());

        assertTrue(debts.isEmpty());
    }

    @Test
    public void simplifyDebts_handlesPartialSettlement() {
        List<SharedExpenseEntity> expenses = Collections.singletonList(
                new SharedExpenseEntity(1, 1, "Food", 100.0, 1L, 0L)
        );
        List<ExpenseShareEntity> shares = Arrays.asList(
                new ExpenseShareEntity(1, 1L, 50.0),
                new ExpenseShareEntity(1, 2L, 50.0)
        );
        List<SettlementEntity> settlements = Collections.singletonList(
                new SettlementEntity(1, 1, 2L, 1L, 20.0, 0L)
        );

        List<Debt> debts = DebtSimplifier.simplifyDebts(expenses, shares, settlements);

        assertEquals(1, debts.size());
        assertEquals(2L, debts.get(0).getFromId());
        assertEquals(1L, debts.get(0).getToId());
        assertEquals(30.0, debts.get(0).getAmount(), 0.01);
    }

    @Test
    public void simplifyDebts_simplifiesTransitiveDebt() {
        List<SharedExpenseEntity> expenses = Arrays.asList(
                new SharedExpenseEntity(1, 1, "B paid for A", 200.0, 2L, 0L),
                new SharedExpenseEntity(2, 1, "C paid for B", 200.0, 3L, 0L)
        );
        List<ExpenseShareEntity> shares = Arrays.asList(
                new ExpenseShareEntity(1, 1L, 200.0),
                new ExpenseShareEntity(2, 2L, 200.0)
        );

        List<Debt> debts = DebtSimplifier.simplifyDebts(expenses, shares, Collections.emptyList());

        assertEquals(1, debts.size());
        assertEquals(1L, debts.get(0).getFromId());
        assertEquals(3L, debts.get(0).getToId());
        assertEquals(200.0, debts.get(0).getAmount(), 0.01);
    }

    @Test
    public void simplifyDebts_handlesComplex4PersonScenario() {
        List<SharedExpenseEntity> expenses = Arrays.asList(
                new SharedExpenseEntity(1, 1, "Hotel", 4000.0, 2L, 0L),
                new SharedExpenseEntity(2, 1, "Dinner", 1200.0, 1L, 0L)
        );
        List<ExpenseShareEntity> shares = Arrays.asList(
                new ExpenseShareEntity(1, 1L, 1000.0),
                new ExpenseShareEntity(1, 2L, 1000.0),
                new ExpenseShareEntity(1, 3L, 1000.0),
                new ExpenseShareEntity(1, 4L, 1000.0),
                new ExpenseShareEntity(2, 1L, 600.0),
                new ExpenseShareEntity(2, 3L, 600.0)
        );

        List<Debt> debts = DebtSimplifier.simplifyDebts(expenses, shares, Collections.emptyList());

        assertEquals(3, debts.size());
        for (Debt d : debts) {
            assertEquals(2L, d.getToId());
        }

        double totalDebtAmount = 0.0;
        for (Debt d : debts) {
            totalDebtAmount += d.getAmount();
        }
        assertEquals(3000.0, totalDebtAmount, 0.01);
    }
}
