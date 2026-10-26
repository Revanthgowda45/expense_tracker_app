package com.example.studentexpensetracker.data.repository

import com.example.studentexpensetracker.data.local.dao.SplitDao
import com.example.studentexpensetracker.data.local.dao.TransactionDao
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.local.entity.split.GroupMemberEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplitRepository @Inject constructor(
    private val splitDao: SplitDao,
    private val transactionDao: TransactionDao
) {
    fun getAllGroups(): Flow<List<ExpenseGroupEntity>> = splitDao.getAllGroups()
    
    fun getGroupById(groupId: Long): Flow<ExpenseGroupEntity?> = splitDao.getGroupById(groupId)

    fun getGroupMembers(groupId: Long): Flow<List<FriendEntity>> = splitDao.getGroupMembers(groupId)

    fun getExpensesForGroup(groupId: Long): Flow<List<SharedExpenseEntity>> = splitDao.getExpensesForGroup(groupId)

    fun getSettlementsForGroupFlow(groupId: Long): Flow<List<SettlementEntity>> = splitDao.getSettlementsForGroupFlow(groupId)

    fun getAllSharesForGroupFlow(groupId: Long): Flow<List<ExpenseShareEntity>> = splitDao.getAllSharesForGroupFlow(groupId)

    suspend fun createGroupWithMembers(name: String, description: String?, friends: List<FriendEntity>): Long {
        val groupId = splitDao.insertGroup(
            ExpenseGroupEntity(name = name.trim(), description = description?.trim(), createdAt = System.currentTimeMillis())
        )
        friends.forEach { friend ->
            val friendId = splitDao.insertFriend(friend)
            splitDao.insertGroupMember(GroupMemberEntity(groupId = groupId, friendId = friendId))
        }
        return groupId
    }
    
    suspend fun updateGroup(group: ExpenseGroupEntity) {
        splitDao.updateGroup(group)
    }
    
    suspend fun addFriendToExistingGroup(groupId: Long, name: String, phoneNumber: String? = null) {
        val friendId = splitDao.insertFriend(FriendEntity(name = name.trim(), phoneNumber = phoneNumber?.trim()))
        splitDao.insertGroupMember(GroupMemberEntity(groupId = groupId, friendId = friendId))
    }
    
    suspend fun deleteGroup(group: ExpenseGroupEntity) {
        splitDao.deleteGroup(group)
    }

    suspend fun addSharedExpense(
        expense: SharedExpenseEntity, 
        shares: List<ExpenseShareEntity>,
        addUserShareToPersonal: Boolean = false,
        userShareAmount: Double = 0.0,
        groupName: String = ""
    ) {
        splitDao.createSharedExpense(expense, shares)

        if (addUserShareToPersonal && userShareAmount > 0.01) {
            val personalTx = TransactionEntity(
                amount = userShareAmount,
                type = "EXPENSE",
                categoryId = 1L, // Default General/Food category
                note = if (groupName.isNotBlank()) "[Split: $groupName] ${expense.description}" else "[Split] ${expense.description}",
                paymentMethod = "Cash/UPI",
                dateTime = expense.dateTime,
                isRecurring = false,
                recurrenceType = null,
                createdAt = System.currentTimeMillis()
            )
            transactionDao.insertTransaction(personalTx)
        }
    }
    
    suspend fun updateSharedExpense(
        expense: SharedExpenseEntity,
        shares: List<ExpenseShareEntity>
    ) {
        splitDao.updateSharedExpense(expense, shares)
    }

    suspend fun getSharesForExpense(expenseId: Long): List<ExpenseShareEntity> {
        return splitDao.getSharesForExpense(expenseId)
    }

    suspend fun deleteSharedExpense(expense: SharedExpenseEntity) {
        splitDao.deleteSharedExpense(expense)
    }

    suspend fun addSettlement(settlement: SettlementEntity) {
        splitDao.insertSettlement(settlement)
    }

    suspend fun getAllSharesForGroup(groupId: Long): List<ExpenseShareEntity> = splitDao.getAllSharesForGroup(groupId)
    
    suspend fun getAllExpensesForGroup(groupId: Long): List<SharedExpenseEntity> = splitDao.getAllExpensesForGroup(groupId)
    
    suspend fun getSettlementsForGroup(groupId: Long): List<SettlementEntity> = splitDao.getSettlementsForGroup(groupId)
}
