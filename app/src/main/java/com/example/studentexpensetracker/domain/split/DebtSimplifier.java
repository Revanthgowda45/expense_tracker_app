package com.example.studentexpensetracker.domain.split;

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class DebtSimplifier {

    public static List<Debt> simplifyDebts(
            List<SharedExpenseEntity> expenses,
            List<ExpenseShareEntity> shares,
            List<SettlementEntity> settlements) {
        Map<Long, Double> balances = new HashMap<>();

        if (expenses != null) {
            for (SharedExpenseEntity expense : expenses) {
                long payerId = expense.getPaidByFriendId();
                balances.put(payerId, balances.getOrDefault(payerId, 0.0) + expense.getAmount());

                if (shares != null) {
                    for (ExpenseShareEntity share : shares) {
                        if (share.getSharedExpenseId() == expense.getSharedExpenseId()) {
                            long memberId = share.getFriendId();
                            balances.put(memberId, balances.getOrDefault(memberId, 0.0) - share.getOwedAmount());
                        }
                    }
                }
            }
        }

        if (settlements != null) {
            for (SettlementEntity settlement : settlements) {
                long from = settlement.getFromFriendId();
                long to = settlement.getToFriendId();
                balances.put(from, balances.getOrDefault(from, 0.0) + settlement.getAmount());
                balances.put(to, balances.getOrDefault(to, 0.0) - settlement.getAmount());
            }
        }

        return calculateMinimumTransactions(balances);
    }

    private static class PersonBalance {
        final long id;
        final double balance;

        PersonBalance(long id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    private static List<Debt> calculateMinimumTransactions(Map<Long, Double> balances) {
        PriorityQueue<PersonBalance> debtors = new PriorityQueue<>((a, b) -> Double.compare(b.balance, a.balance));
        PriorityQueue<PersonBalance> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.balance, a.balance));

        for (Map.Entry<Long, Double> entry : balances.entrySet()) {
            double balance = entry.getValue();
            if (balance < -0.01) {
                debtors.add(new PersonBalance(entry.getKey(), -balance));
            } else if (balance > 0.01) {
                creditors.add(new PersonBalance(entry.getKey(), balance));
            }
        }

        List<Debt> simplifiedDebts = new ArrayList<>();

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            PersonBalance debtor = debtors.poll();
            PersonBalance creditor = creditors.poll();
            if (debtor == null || creditor == null) break;

            double rawSettlement = Math.min(debtor.balance, creditor.balance);
            double settlementAmount = Math.round(rawSettlement * 100.0) / 100.0;

            if (settlementAmount > 0.01) {
                simplifiedDebts.add(new Debt(debtor.id, creditor.id, settlementAmount));
            }

            double remainingDebtor = Math.round((debtor.balance - settlementAmount) * 100.0) / 100.0;
            double remainingCreditor = Math.round((creditor.balance - settlementAmount) * 100.0) / 100.0;

            if (remainingDebtor > 0.01) {
                debtors.add(new PersonBalance(debtor.id, remainingDebtor));
            }
            if (remainingCreditor > 0.01) {
                creditors.add(new PersonBalance(creditor.id, remainingCreditor));
            }
        }

        return simplifiedDebts;
    }
}
