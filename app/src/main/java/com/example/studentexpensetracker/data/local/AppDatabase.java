package com.example.studentexpensetracker.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.studentexpensetracker.data.local.dao.CategoryDao;
import com.example.studentexpensetracker.data.local.dao.SplitDao;
import com.example.studentexpensetracker.data.local.dao.TransactionDao;
import com.example.studentexpensetracker.data.local.entity.CategoryEntity;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseGroupEntity;
import com.example.studentexpensetracker.data.local.entity.split.ExpenseShareEntity;
import com.example.studentexpensetracker.data.local.entity.split.FriendEntity;
import com.example.studentexpensetracker.data.local.entity.split.GroupMemberEntity;
import com.example.studentexpensetracker.data.local.entity.split.SettlementEntity;
import com.example.studentexpensetracker.data.local.entity.split.SharedExpenseEntity;

@Database(
    entities = {
        TransactionEntity.class,
        CategoryEntity.class,
        ExpenseGroupEntity.class,
        FriendEntity.class,
        GroupMemberEntity.class,
        SharedExpenseEntity.class,
        ExpenseShareEntity.class,
        SettlementEntity.class
    },
    version = 4,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();
    public abstract SplitDao splitDao();

    private static volatile AppDatabase sInstance;

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN mood TEXT");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `expense_groups` (" +
                    "`groupId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`description` TEXT, " +
                    "`createdAt` INTEGER NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `friends` (" +
                    "`friendId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`phoneNumber` TEXT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `group_members` (" +
                    "`groupId` INTEGER NOT NULL, " +
                    "`friendId` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`groupId`, `friendId`), " +
                    "FOREIGN KEY(`groupId`) REFERENCES `expense_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`friendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `shared_expenses` (" +
                    "`sharedExpenseId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`groupId` INTEGER NOT NULL, " +
                    "`description` TEXT NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`paidByFriendId` INTEGER NOT NULL, " +
                    "`dateTime` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`groupId`) REFERENCES `expense_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`paidByFriendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE RESTRICT)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `expense_shares` (" +
                    "`sharedExpenseId` INTEGER NOT NULL, " +
                    "`friendId` INTEGER NOT NULL, " +
                    "`owedAmount` REAL NOT NULL, " +
                    "PRIMARY KEY(`sharedExpenseId`, `friendId`), " +
                    "FOREIGN KEY(`sharedExpenseId`) REFERENCES `shared_expenses`(`sharedExpenseId`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`friendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `settlements` (" +
                    "`settlementId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`groupId` INTEGER NOT NULL, " +
                    "`fromFriendId` INTEGER NOT NULL, " +
                    "`toFriendId` INTEGER NOT NULL, " +
                    "`amount` REAL NOT NULL, " +
                    "`dateTime` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`groupId`) REFERENCES `expense_groups`(`groupId`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                    "FOREIGN KEY(`fromFriendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE RESTRICT, " +
                    "FOREIGN KEY(`toFriendId`) REFERENCES `friends`(`friendId`) ON UPDATE NO ACTION ON DELETE RESTRICT)");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN receiptImagePath TEXT");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "expense_tracker_db"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return sInstance;
    }
}
