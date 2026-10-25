package com.example.studentexpensetracker.di

import android.app.Application
import androidx.room.Room
import com.example.studentexpensetracker.data.local.AppDatabase
import com.example.studentexpensetracker.data.local.dao.CategoryDao
import com.example.studentexpensetracker.data.local.dao.SplitDao
import com.example.studentexpensetracker.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "student_expense_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao {
        return db.transactionDao
    }

    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao {
        return db.categoryDao
    }

    @Provides
    @Singleton
    fun provideSplitDao(db: AppDatabase): SplitDao {
        return db.splitDao
    }
}
