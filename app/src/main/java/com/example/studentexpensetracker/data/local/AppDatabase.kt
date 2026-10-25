package com.example.studentexpensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.studentexpensetracker.data.local.dao.CategoryDao
import com.example.studentexpensetracker.data.local.dao.SplitDao
import com.example.studentexpensetracker.data.local.dao.TransactionDao
import com.example.studentexpensetracker.data.local.entity.CategoryEntity
import com.example.studentexpensetracker.data.local.entity.TransactionEntity
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity
import com.example.studentexpensetracker.data.local.entity.split.GroupMemberEntity
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity

@Database(
    entities = [
        TransactionEntity::class, 
        CategoryEntity::class,
        ExpenseGroupEntity::class,
        FriendEntity::class,
        GroupMemberEntity::class,
        SharedExpenseEntity::class,
        ExpenseShareEntity::class,
        SettlementEntity::class
    ],
    version = 3, // Incremented for Split feature
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val splitDao: SplitDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN mood TEXT")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Creates done via Room's fallbackToDestructiveMigration if preferred, 
                // but we supply the explicit creates for safety.
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expense_groups` (
                        `groupId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `description` TEXT, 
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `friends` (
                        `friendId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `phoneNumber` TEXT
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `group_members` (
                        `groupId` INTEGER NOT NULL, 
                        `friendId` INTEGER NOT NULL, 
                        PRIMARY KEY(`groupId`, `friendId`), 
                        FOREIGN KEY(`groupId`) REFERENCES `expense_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`friendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shared_expenses` (
                        `sharedExpenseId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `groupId` INTEGER NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `paidByFriendId` INTEGER NOT NULL, 
                        `dateTime` INTEGER NOT NULL, 
                        FOREIGN KEY(`groupId`) REFERENCES `expense_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`paidByFriendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE RESTRICT 
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `expense_shares` (
                        `sharedExpenseId` INTEGER NOT NULL, 
                        `friendId` INTEGER NOT NULL, 
                        `owedAmount` REAL NOT NULL, 
                        PRIMARY KEY(`sharedExpenseId`, `friendId`), 
                        FOREIGN KEY(`sharedExpenseId`) REFERENCES `shared_expenses`(`sharedExpenseId`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`friendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `settlements` (
                        `settlementId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `groupId` INTEGER NOT NULL, 
                        `fromFriendId` INTEGER NOT NULL, 
                        `toFriendId` INTEGER NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `dateTime` INTEGER NOT NULL, 
                        FOREIGN KEY(`groupId`) REFERENCES `expense_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE CASCADE , 
                        FOREIGN KEY(`fromFriendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE RESTRICT , 
                        FOREIGN KEY(`toFriendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE RESTRICT 
                    )
                """)
            }
        }
    }
}
