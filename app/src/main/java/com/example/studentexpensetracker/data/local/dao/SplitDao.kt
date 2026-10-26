package com.example.studentexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.local.entity.split.GroupMemberEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ExpenseGroupEntity): Long
    
    @Update
    suspend fun updateGroup(group: ExpenseGroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity)

    @Delete
    suspend fun deleteGroup(group: ExpenseGroupEntity)

    @Query("SELECT * FROM expense_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<ExpenseGroupEntity>>

    @Query("""
        SELECT f.* FROM friends f
        INNER JOIN group_members gm ON f.friendId = gm.friendId
        WHERE gm.groupId = :groupId
    """)
    fun getGroupMembers(groupId: Long): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedExpense(expense: SharedExpenseEntity): Long
    
    @Delete
    suspend fun deleteSharedExpense(expense: SharedExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseShares(shares: List<ExpenseShareEntity>)

    @Transaction
    suspend fun createSharedExpense(expense: SharedExpenseEntity, shares: List<ExpenseShareEntity>) {
        val id = insertSharedExpense(expense)
        insertExpenseShares(shares.map { it.copy(sharedExpenseId = id) })
    }

    @Query("DELETE FROM expense_shares WHERE sharedExpenseId = :expenseId")
    suspend fun deleteSharesForExpense(expenseId: Long)

    @Transaction
    suspend fun updateSharedExpense(expense: SharedExpenseEntity, shares: List<ExpenseShareEntity>) {
        insertSharedExpense(expense)
        deleteSharesForExpense(expense.sharedExpenseId)
        insertExpenseShares(shares.map { it.copy(sharedExpenseId = expense.sharedExpenseId) })
    }

    @Query("SELECT * FROM shared_expenses WHERE groupId = :groupId ORDER BY dateTime DESC")
    fun getExpensesForGroup(groupId: Long): Flow<List<SharedExpenseEntity>>

    @Query("SELECT * FROM expense_shares WHERE sharedExpenseId = :expenseId")
    suspend fun getSharesForExpense(expenseId: Long): List<ExpenseShareEntity>

    @Query("SELECT * FROM expense_shares WHERE sharedExpenseId IN (SELECT sharedExpenseId FROM shared_expenses WHERE groupId = :groupId)")
    suspend fun getAllSharesForGroup(groupId: Long): List<ExpenseShareEntity>
    
    @Query("SELECT * FROM shared_expenses WHERE groupId = :groupId")
    suspend fun getAllExpensesForGroup(groupId: Long): List<SharedExpenseEntity>

    @Query("SELECT * FROM expense_groups WHERE groupId = :groupId")
    fun getGroupById(groupId: Long): Flow<ExpenseGroupEntity?>

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY dateTime DESC")
    fun getSettlementsForGroupFlow(groupId: Long): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM expense_shares WHERE sharedExpenseId IN (SELECT sharedExpenseId FROM shared_expenses WHERE groupId = :groupId)")
    fun getAllSharesForGroupFlow(groupId: Long): Flow<List<ExpenseShareEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity)
    
    @Query("SELECT * FROM settlements WHERE groupId = :groupId")
    suspend fun getSettlementsForGroup(groupId: Long): List<SettlementEntity>
}
