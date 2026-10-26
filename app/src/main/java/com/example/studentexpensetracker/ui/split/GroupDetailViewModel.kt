package com.example.studentexpensetracker.ui.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import com.example.studentexpensetracker.data.repository.SplitRepository
import com.example.studentexpensetracker.domain.split.Debt
import com.example.studentexpensetracker.domain.split.DebtSimplifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemberBalanceSummary(
    val friend: FriendEntity,
    val totalPaid: Double,
    val totalShare: Double,
    val netBalance: Double // Positive = owed to member, Negative = member owes
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val splitRepository: SplitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    val group: StateFlow<ExpenseGroupEntity?> = splitRepository.getGroupById(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val groupMembers: StateFlow<List<FriendEntity>> = splitRepository.getGroupMembers(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupExpenses: StateFlow<List<SharedExpenseEntity>> = splitRepository.getExpensesForGroup(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settlements: StateFlow<List<SettlementEntity>> = splitRepository.getSettlementsForGroupFlow(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShares: StateFlow<List<ExpenseShareEntity>> = splitRepository.getAllSharesForGroupFlow(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGroupSpending: StateFlow<Double> = groupExpenses.map { expenses ->
        expenses.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val simplifiedDebts: StateFlow<List<Debt>> = combine(
        groupExpenses,
        allShares,
        settlements
    ) { expenses, shares, settlementList ->
        DebtSimplifier.simplifyDebts(expenses, shares, settlementList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberSummaries: StateFlow<List<MemberBalanceSummary>> = combine(
        groupMembers,
        groupExpenses,
        allShares,
        settlements
    ) { members, expenses, shares, settlementList ->
        members.map { member ->
            val paid = expenses.filter { it.paidByFriendId == member.friendId }.sumOf { it.amount }
            val share = shares.filter { it.friendId == member.friendId }.sumOf { it.owedAmount }
            val settlementsReceived = settlementList.filter { it.toFriendId == member.friendId }.sumOf { it.amount }
            val settlementsPaid = settlementList.filter { it.fromFriendId == member.friendId }.sumOf { it.amount }

            val net = kotlin.math.round(((paid - share) + (settlementsPaid - settlementsReceived)) * 100.0) / 100.0

            MemberBalanceSummary(
                friend = member,
                totalPaid = kotlin.math.round(paid * 100.0) / 100.0,
                totalShare = kotlin.math.round(share * 100.0) / 100.0,
                netBalance = net
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFriendToGroup(name: String, phoneNumber: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            splitRepository.addFriendToExistingGroup(groupId, name.trim(), phoneNumber?.trim())
        }
    }

    fun addSharedExpense(
        description: String,
        totalAmount: Double,
        paidByFriendId: Long,
        shares: List<ExpenseShareEntity>,
        addUserShareToPersonal: Boolean
    ) {
        if (shares.isEmpty() || totalAmount <= 0) return

        viewModelScope.launch {
            val roundedTotal = kotlin.math.round(totalAmount * 100.0) / 100.0
            val expense = SharedExpenseEntity(
                groupId = groupId,
                description = description.trim().ifBlank { "Shared Expense" },
                amount = roundedTotal,
                paidByFriendId = paidByFriendId,
                dateTime = System.currentTimeMillis()
            )

            // Find user's friend ID ("You")
            val userFriend = groupMembers.value.firstOrNull { it.name.equals("You", ignoreCase = true) }
            val userShare = userFriend?.let { uf ->
                shares.firstOrNull { it.friendId == uf.friendId }?.owedAmount ?: 0.0
            } ?: 0.0

            val currentGroupName = group.value?.name ?: "Group"

            splitRepository.addSharedExpense(
                expense = expense,
                shares = shares,
                addUserShareToPersonal = addUserShareToPersonal,
                userShareAmount = userShare,
                groupName = currentGroupName
            )
        }
    }

    fun updateSharedExpense(
        expenseId: Long,
        description: String,
        totalAmount: Double,
        paidByFriendId: Long,
        shares: List<ExpenseShareEntity>
    ) {
        if (shares.isEmpty() || totalAmount <= 0) return

        viewModelScope.launch {
            val roundedTotal = kotlin.math.round(totalAmount * 100.0) / 100.0
            val existingExpense = groupExpenses.value.firstOrNull { it.sharedExpenseId == expenseId }
            val expense = SharedExpenseEntity(
                sharedExpenseId = expenseId,
                groupId = groupId,
                description = description.trim().ifBlank { "Shared Expense" },
                amount = roundedTotal,
                paidByFriendId = paidByFriendId,
                dateTime = existingExpense?.dateTime ?: System.currentTimeMillis()
            )
            splitRepository.updateSharedExpense(expense, shares)
        }
    }

    fun deleteSharedExpense(expense: SharedExpenseEntity) {
        viewModelScope.launch {
            splitRepository.deleteSharedExpense(expense)
        }
    }

    fun settleDebt(fromId: Long, toId: Long, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            val settlement = SettlementEntity(
                groupId = groupId,
                fromFriendId = fromId,
                toFriendId = toId,
                amount = kotlin.math.round(amount * 100.0) / 100.0,
                dateTime = System.currentTimeMillis()
            )
            splitRepository.addSettlement(settlement)
        }
    }
}
