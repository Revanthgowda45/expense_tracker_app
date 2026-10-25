package com.example.studentexpensetracker.domain.split

import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import java.util.PriorityQueue

data class Debt(val fromId: Long, val toId: Long, val amount: Double)

object DebtSimplifier {
    
    fun simplifyDebts(
        expenses: List<SharedExpenseEntity>,
        shares: List<ExpenseShareEntity>,
        settlements: List<SettlementEntity>
    ): List<Debt> {
        // Net balance for each person: Positive means they should receive money, negative means they owe money
        val balances = mutableMapOf<Long, Double>()

        // 1. Process Expenses
        expenses.forEach { expense ->
            val expenseShares = shares.filter { it.sharedExpenseId == expense.sharedExpenseId }
            
            // The person who paid gets a positive balance for the full amount
            balances[expense.paidByFriendId] = (balances[expense.paidByFriendId] ?: 0.0) + expense.amount
            
            // Everyone involved (including the payer if they split it) gets a negative balance for their share
            expenseShares.forEach { share ->
                balances[share.friendId] = (balances[share.friendId] ?: 0.0) - share.owedAmount
            }
        }

        // 2. Process Settlements
        settlements.forEach { settlement ->
            // Person paying GAINS balance because they paid off their debt.
            balances[settlement.fromFriendId] = (balances[settlement.fromFriendId] ?: 0.0) + settlement.amount
            // Person receiving LOSES balance because their credit was paid back.
            balances[settlement.toFriendId] = (balances[settlement.toFriendId] ?: 0.0) - settlement.amount
        }

        // 3. Simplify
        return calculateMinimumTransactions(balances)
    }

    private fun calculateMinimumTransactions(balances: Map<Long, Double>): List<Debt> {
        val debtors = PriorityQueue<Pair<Long, Double>>(compareByDescending { it.second }) 
        val creditors = PriorityQueue<Pair<Long, Double>>(compareByDescending { it.second })

        balances.forEach { (id, balance) ->
            if (balance < -0.01) {
                debtors.add(id to -balance) // Storing absolute value for ease
            } else if (balance > 0.01) {
                creditors.add(id to balance)
            }
        }

        val simplifiedDebts = mutableListOf<Debt>()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val debtor = debtors.poll()!!
            val creditor = creditors.poll()!!

            val settlementAmount = minOf(debtor.second, creditor.second)
            
            simplifiedDebts.add(Debt(fromId = debtor.first, toId = creditor.first, amount = settlementAmount))

            val remainingDebtorAmount = debtor.second - settlementAmount
            val remainingCreditorAmount = creditor.second - settlementAmount

            if (remainingDebtorAmount > 0.01) {
                debtors.add(debtor.first to remainingDebtorAmount)
            }
            if (remainingCreditorAmount > 0.01) {
                creditors.add(creditor.first to remainingCreditorAmount)
            }
        }

        return simplifiedDebts
    }
}
