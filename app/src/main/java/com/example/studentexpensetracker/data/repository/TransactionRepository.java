package com.example.studentexpensetracker.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.studentexpensetracker.data.local.AppDatabase;
import com.example.studentexpensetracker.data.local.AppExecutors;
import com.example.studentexpensetracker.data.local.dao.TransactionDao;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;

import java.util.List;

public class TransactionRepository {

    private static volatile TransactionRepository sInstance;
    private final TransactionDao transactionDao;
    private final AppExecutors executors;

    private TransactionRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.transactionDao = db.transactionDao();
        this.executors = AppExecutors.getInstance();
    }

    public static TransactionRepository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TransactionRepository.class) {
                if (sInstance == null) {
                    sInstance = new TransactionRepository(context);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public List<TransactionEntity> getAllTransactionsSync() {
        return transactionDao.getAllTransactionsSync();
    }

    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    public Double getTotalIncomeSync() {
        return transactionDao.getTotalIncomeSync();
    }

    public LiveData<Double> getTotalExpense() {
        return transactionDao.getTotalExpense();
    }

    public Double getTotalExpenseSync() {
        return transactionDao.getTotalExpenseSync();
    }

    public LiveData<Double> getExpenseSumSince(long sinceTimestamp) {
        return transactionDao.getExpenseSumSince(sinceTimestamp);
    }

    public Double getExpenseSumSinceSync(long sinceTimestamp) {
        return transactionDao.getExpenseSumSinceSync(sinceTimestamp);
    }

    public LiveData<TransactionEntity> getTransactionById(long id) {
        return transactionDao.getTransactionById(id);
    }

    public TransactionEntity getTransactionByIdSync(long id) {
        return transactionDao.getTransactionByIdSync(id);
    }

    public void insertTransaction(TransactionEntity transaction, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            transactionDao.insertTransaction(transaction);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    public void insertTransaction(TransactionEntity transaction) {
        insertTransaction(transaction, null);
    }

    public void updateTransaction(TransactionEntity transaction, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            transactionDao.updateTransaction(transaction);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    public void updateTransaction(TransactionEntity transaction) {
        updateTransaction(transaction, null);
    }

    public void deleteTransaction(TransactionEntity transaction, Runnable onComplete) {
        executors.diskIO().execute(() -> {
            transactionDao.deleteTransaction(transaction);
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    public void deleteTransaction(TransactionEntity transaction) {
        deleteTransaction(transaction, null);
    }

    public void clearAllTransactions(Runnable onComplete) {
        executors.diskIO().execute(() -> {
            transactionDao.clearAllTransactions();
            if (onComplete != null) {
                executors.mainThread().execute(onComplete);
            }
        });
    }

    public void clearAllTransactions() {
        clearAllTransactions(null);
    }
}
