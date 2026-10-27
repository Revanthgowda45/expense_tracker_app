package com.example.studentexpensetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.studentexpensetracker.data.local.AppDatabase;
import com.example.studentexpensetracker.data.local.AppExecutors;
import com.example.studentexpensetracker.data.local.dao.SplitDao;
import com.example.studentexpensetracker.data.local.dao.TransactionDao;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.data.local.entity.split.GroupMemberEntity;
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;

import java.util.List;

public class SplitRepository {

    private static volatile SplitRepository sInstance;
    private final SplitDao splitDao;
    private final TransactionDao transactionDao;
    private final AppExecutors executors;

    public interface Callback<T> {
        void onResult(T result);
    }

    private SplitRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.splitDao = db.splitDao();
        this.transactionDao = db.transactionDao();
        this.executors = AppExecutors.getInstance();
    }

    public static SplitRepository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SplitRepository.class) {
                if (sInstance == null) {
                    sInstance = new SplitRepository(context);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<ExpenseGroupEntity>> getAllGroups() {
        return splitDao.getAllGroups();
    }

    public LiveData<ExpenseGroupEntity> getGroupById(long groupId) {
        return splitDao.getGroupById(groupId);
    }

    public LiveData<List<FriendEntity>> getGroupMembers(long groupId) {
        return splitDao.getGroupMembers(groupId);
    }

    public List<FriendEntity> getGroupMembersSync(long groupId) {
        return splitDao.getGroupMembersSync(groupId);
    }

    public LiveData<List<SharedExpenseEntity>> getExpensesForGroup(long groupId) {
        return splitDao.getExpensesForGroup(groupId);
    }

    public LiveData<List<SettlementEntity>> getSettlementsForGroupFlow(long groupId) {
        return splitDao.getSettlementsForGroupFlow(groupId);
    }

    public LiveData<List<ExpenseShareEntity>> getAllSharesForGroupFlow(long groupId) {
        return splitDao.getAllSharesForGroupFlow(groupId);
    }

    public void createGroupWithMembers(String name, String description, List<FriendEntity> friends, Callback<Long> onComplete) {
        executors.diskIO().execute(() -> {
            long groupId = splitDao.insertGroup(new ExpenseGroupEntity(
                    name != null ? name.trim() : "",
                    description != null ? description.trim() : null,
                    System.currentTimeMillis()
            ));

            if (friends != null) {
                for (FriendEntity friend : friends) {
                    long friendId = splitDao.insertFriend(friend);
                    splitDao.insertGroupMember(new GroupMemberEntity(groupId, friendId));
                }
            }

            if (onComplete != null) {
                executors.mainThread().execute(() -> onComplete.onResult(groupId));
            }
        });
    }

    public void updateGroup(ExpenseGroupEntity group) {
        executors.diskIO().execute(() -> splitDao.updateGroup(group));
    }

    public void addFriendToExistingGroup(long groupId, String name, String phoneNumber) {
        executors.diskIO().execute(() -> {
            long friendId = splitDao.insertFriend(new FriendEntity(
                    name != null ? name.trim() : "",
                    phoneNumber != null ? phoneNumber.trim() : null
            ));
            splitDao.insertGroupMember(new GroupMemberEntity(groupId, friendId));
        });
    }

    public void deleteGroup(ExpenseGroupEntity group) {
        executors.diskIO().execute(() -> splitDao.deleteGroup(group));
    }

    public void addSharedExpense(
            SharedExpenseEntity expense,
            List<ExpenseShareEntity> shares,
            boolean addUserShareToPersonal,
            double userShareAmount,
            String groupName) {
        executors.diskIO().execute(() -> {
            splitDao.createSharedExpense(expense, shares);

            if (addUserShareToPersonal && userShareAmount > 0.01) {
                String note = (groupName != null && !groupName.trim().isEmpty())
                        ? "[Split: " + groupName + "] " + expense.getDescription()
                        : "[Split] " + expense.getDescription();

                TransactionEntity personalTx = new TransactionEntity(
                        userShareAmount,
                        "EXPENSE",
                        1L,
                        note,
                        "Cash/UPI",
                        expense.getDateTime(),
                        false,
                        null,
                        System.currentTimeMillis(),
                        null,
                        null
                );
                transactionDao.insertTransaction(personalTx);
            }
        });
    }

    public void updateSharedExpense(SharedExpenseEntity expense, List<ExpenseShareEntity> shares) {
        executors.diskIO().execute(() -> splitDao.updateSharedExpense(expense, shares));
    }

    public void deleteSharedExpense(SharedExpenseEntity expense) {
        executors.diskIO().execute(() -> splitDao.deleteSharedExpense(expense));
    }

    public void addSettlement(SettlementEntity settlement) {
        executors.diskIO().execute(() -> splitDao.insertSettlement(settlement));
    }

    public List<ExpenseShareEntity> getAllSharesForGroup(long groupId) {
        return splitDao.getAllSharesForGroup(groupId);
    }

    public List<SharedExpenseEntity> getAllExpensesForGroup(long groupId) {
        return splitDao.getAllExpensesForGroup(groupId);
    }

    public List<SettlementEntity> getSettlementsForGroup(long groupId) {
        return splitDao.getSettlementsForGroup(groupId);
    }
}
