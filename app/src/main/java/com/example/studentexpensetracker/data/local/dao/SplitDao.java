package com.example.studentexpensetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.data.local.entity.split.GroupMemberEntity;
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SplitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGroup(ExpenseGroupEntity group);

    @Update
    void updateGroup(ExpenseGroupEntity group);

    @Delete
    void deleteGroup(ExpenseGroupEntity group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertFriend(FriendEntity friend);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroupMember(GroupMemberEntity member);

    @Query("SELECT * FROM expense_groups ORDER BY createdAt DESC")
    LiveData<List<ExpenseGroupEntity>> getAllGroups();

    @Query("SELECT * FROM expense_groups WHERE groupId = :groupId")
    LiveData<ExpenseGroupEntity> getGroupById(long groupId);

    @Query("SELECT f.* FROM friends f " +
           "INNER JOIN group_members gm ON f.friendId = gm.friendId " +
           "WHERE gm.groupId = :groupId")
    LiveData<List<FriendEntity>> getGroupMembers(long groupId);

    @Query("SELECT f.* FROM friends f " +
           "INNER JOIN group_members gm ON f.friendId = gm.friendId " +
           "WHERE gm.groupId = :groupId")
    List<FriendEntity> getGroupMembersSync(long groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSharedExpense(SharedExpenseEntity expense);

    @Delete
    void deleteSharedExpense(SharedExpenseEntity expense);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExpenseShares(List<ExpenseShareEntity> shares);

    @Query("DELETE FROM expense_shares WHERE sharedExpenseId = :expenseId")
    void deleteSharesForExpense(long expenseId);

    @Transaction
    default void createSharedExpense(SharedExpenseEntity expense, List<ExpenseShareEntity> shares) {
        long id = insertSharedExpense(expense);
        if (shares != null) {
            List<ExpenseShareEntity> updatedShares = new ArrayList<>();
            for (ExpenseShareEntity s : shares) {
                updatedShares.add(new ExpenseShareEntity(id, s.getFriendId(), s.getOwedAmount()));
            }
            insertExpenseShares(updatedShares);
        }
    }

    @Transaction
    default void updateSharedExpense(SharedExpenseEntity expense, List<ExpenseShareEntity> shares) {
        insertSharedExpense(expense);
        deleteSharesForExpense(expense.getSharedExpenseId());
        if (shares != null) {
            List<ExpenseShareEntity> updatedShares = new ArrayList<>();
            for (ExpenseShareEntity s : shares) {
                updatedShares.add(new ExpenseShareEntity(expense.getSharedExpenseId(), s.getFriendId(), s.getOwedAmount()));
            }
            insertExpenseShares(updatedShares);
        }
    }

    @Query("SELECT * FROM shared_expenses WHERE groupId = :groupId ORDER BY dateTime DESC")
    LiveData<List<SharedExpenseEntity>> getExpensesForGroup(long groupId);

    @Query("SELECT * FROM expense_shares WHERE sharedExpenseId = :expenseId")
    List<ExpenseShareEntity> getSharesForExpense(long expenseId);

    @Query("SELECT * FROM expense_shares WHERE sharedExpenseId IN (SELECT sharedExpenseId FROM shared_expenses WHERE groupId = :groupId)")
    List<ExpenseShareEntity> getAllSharesForGroup(long groupId);

    @Query("SELECT * FROM expense_shares WHERE sharedExpenseId IN (SELECT sharedExpenseId FROM shared_expenses WHERE groupId = :groupId)")
    LiveData<List<ExpenseShareEntity>> getAllSharesForGroupFlow(long groupId);

    @Query("SELECT * FROM shared_expenses WHERE groupId = :groupId")
    List<SharedExpenseEntity> getAllExpensesForGroup(long groupId);

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY dateTime DESC")
    LiveData<List<SettlementEntity>> getSettlementsForGroupFlow(long groupId);

    @Query("SELECT * FROM settlements WHERE groupId = :groupId")
    List<SettlementEntity> getSettlementsForGroup(long groupId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSettlement(SettlementEntity settlement);
}
