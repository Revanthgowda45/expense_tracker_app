package com.example.studentexpensetracker.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTransaction(TransactionEntity transaction);

    @Update
    void updateTransaction(TransactionEntity transaction);

    @Delete
    void deleteTransaction(TransactionEntity transaction);

    @Query("DELETE FROM transactions")
    void clearAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    LiveData<List<TransactionEntity>> getAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    List<TransactionEntity> getAllTransactionsSync();

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateTime DESC")
    LiveData<List<TransactionEntity>> getTransactionsByType(String type);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    Double getTotalIncomeSync();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    LiveData<Double> getTotalExpense();

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    Double getTotalExpenseSync();

    @Query("SELECT * FROM transactions WHERE id = :id")
    LiveData<TransactionEntity> getTransactionById(long id);

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getTransactionByIdSync(long id);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND dateTime >= :sinceTimestamp")
    LiveData<Double> getExpenseSumSince(long sinceTimestamp);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND dateTime >= :sinceTimestamp")
    Double getExpenseSumSinceSync(long sinceTimestamp);
}
