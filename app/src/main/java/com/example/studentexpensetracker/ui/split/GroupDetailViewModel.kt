package com.example.studentexpensetracker.ui.split

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import com.example.studentexpensetracker.data.repository.SplitRepository
import com.example.studentexpensetracker.domain.split.Debt
import com.example.studentexpensetracker.domain.split.DebtSimplifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val splitRepository: SplitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    val groupMembers = splitRepository.getGroupMembers(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupExpenses = splitRepository.getExpensesForGroup(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _simplifiedDebts = MutableStateFlow<List<Debt>>(emptyList())
    val simplifiedDebts: StateFlow<List<Debt>> = _simplifiedDebts

    init {
        refreshDebts()
    }

    private fun refreshDebts() {
        viewModelScope.launch {
            val expenses = splitRepository.getAllExpensesForGroup(groupId)
            val shares = splitRepository.getAllSharesForGroup(groupId)
            val settlements = splitRepository.getSettlementsForGroup(groupId)
            
            _simplifiedDebts.value = DebtSimplifier.simplifyDebts(expenses, shares, settlements)
        }
    }

    fun addFriendToGroup(name: String) {
        viewModelScope.launch {
            splitRepository.addFriendToExistingGroup(groupId, name)
            refreshDebts()
        }
    }

    fun addSharedExpense(description: String, totalAmount: Double, paidByFriendId: Long, memberIds: List<Long>) {
        if (memberIds.isEmpty() || totalAmount <= 0) return

        viewModelScope.launch {
            val splitAmount = totalAmount / memberIds.size
            
            val expense = SharedExpenseEntity(
                groupId = groupId,
                description = description,
                amount = totalAmount,
                paidByFriendId = paidByFriendId,
                dateTime = System.currentTimeMillis()
            )
            
            val shares = memberIds.map { memberId ->
                ExpenseShareEntity(
                    sharedExpenseId = 0L, 
                    friendId = memberId,
                    owedAmount = splitAmount
                )
            }
            
            splitRepository.addSharedExpense(expense, shares)
            refreshDebts()
        }
    }
    
    fun deleteSharedExpense(expense: SharedExpenseEntity) {
        viewModelScope.launch {
            splitRepository.deleteSharedExpense(expense)
            refreshDebts()
        }
    }

    fun settleDebt(fromId: Long, toId: Long, amount: Double) {
        viewModelScope.launch {
            val settlement = SettlementEntity(
                groupId = groupId,
                fromFriendId = fromId,
                toFriendId = toId,
                amount = amount,
                dateTime = System.currentTimeMillis()
            )
            splitRepository.addSettlement(settlement)
            refreshDebts()
        }
    }
}
