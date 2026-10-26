package com.example.studentexpensetracker.data.repository

import com.example.studentexpensetracker.data.local.dao.TransactionDao
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    
    fun getTotalIncome(): Flow<Double?> = transactionDao.getTotalIncome()
    
    fun getTotalExpense(): Flow<Double?> = transactionDao.getTotalExpense()
    
    fun getExpenseSumSince(sinceTimestamp: Long): Flow<Double?> = transactionDao.getExpenseSumSince(sinceTimestamp)
    
    fun getTransactionById(id: Long): Flow<TransactionEntity?> = transactionDao.getTransactionById(id)

    suspend fun getTransactionByIdSync(id: Long): TransactionEntity? = transactionDao.getTransactionByIdSync(id)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun clearAllTransactions() {
        transactionDao.clearAllTransactions()
    }
}

