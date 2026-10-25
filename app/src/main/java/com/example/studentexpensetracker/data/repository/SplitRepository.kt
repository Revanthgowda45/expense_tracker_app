package com.example.studentexpensetracker.data.repository

import com.example.studentexpensetracker.data.local.dao.SplitDao
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
    private val splitDao: SplitDao
) {
    fun getAllGroups(): Flow<List<ExpenseGroupEntity>> = splitDao.getAllGroups()
    
    fun getGroupMembers(groupId: Long): Flow<List<FriendEntity>> = splitDao.getGroupMembers(groupId)

    fun getExpensesForGroup(groupId: Long): Flow<List<SharedExpenseEntity>> = splitDao.getExpensesForGroup(groupId)

    suspend fun createGroupWithMembers(name: String, description: String?, friends: List<FriendEntity>) {
        val groupId = splitDao.insertGroup(
            ExpenseGroupEntity(name = name, description = description, createdAt = System.currentTimeMillis())
        )
        friends.forEach { friend ->
            val friendId = splitDao.insertFriend(friend)
            splitDao.insertGroupMember(GroupMemberEntity(groupId = groupId, friendId = friendId))
        }
    }
    
    suspend fun updateGroup(group: ExpenseGroupEntity) {
        splitDao.updateGroup(group)
    }
    
    suspend fun addFriendToExistingGroup(groupId: Long, name: String) {
        val friendId = splitDao.insertFriend(FriendEntity(name = name, phoneNumber = null))
        splitDao.insertGroupMember(GroupMemberEntity(groupId = groupId, friendId = friendId))
    }
    
    suspend fun deleteGroup(group: ExpenseGroupEntity) {
        splitDao.deleteGroup(group)
    }

    suspend fun addSharedExpense(expense: SharedExpenseEntity, shares: List<ExpenseShareEntity>) {
        splitDao.createSharedExpense(expense, shares)
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
