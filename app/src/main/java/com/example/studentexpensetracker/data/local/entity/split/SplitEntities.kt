package com.example.studentexpensetracker.data.local.entity.split

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "expense_groups")
data class ExpenseGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val groupId: Long = 0,
    val name: String,
    val description: String?,
    val createdAt: Long
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val friendId: Long = 0,
    val name: String,
    val phoneNumber: String?
)

@Entity(
    tableName = "group_members",
    primaryKeys = ["groupId", "friendId"],
    foreignKeys = [
        ForeignKey(entity = ExpenseGroupEntity::class, parentColumns = ["groupId"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FriendEntity::class, parentColumns = ["friendId"], childColumns = ["friendId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class GroupMemberEntity(
    val groupId: Long,
    val friendId: Long
)

@Entity(
    tableName = "shared_expenses",
    foreignKeys = [
        ForeignKey(entity = ExpenseGroupEntity::class, parentColumns = ["groupId"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FriendEntity::class, parentColumns = ["friendId"], childColumns = ["paidByFriendId"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class SharedExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val sharedExpenseId: Long = 0,
    val groupId: Long,
    val description: String,
    val amount: Double,
    val paidByFriendId: Long, // Who paid the bill
    val dateTime: Long
)

@Entity(
    tableName = "expense_shares",
    primaryKeys = ["sharedExpenseId", "friendId"],
    foreignKeys = [
        ForeignKey(entity = SharedExpenseEntity::class, parentColumns = ["sharedExpenseId"], childColumns = ["sharedExpenseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FriendEntity::class, parentColumns = ["friendId"], childColumns = ["friendId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ExpenseShareEntity(
    val sharedExpenseId: Long,
    val friendId: Long, // Who owes the money
    val owedAmount: Double // Exact amount this person owes for this expense
)

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(entity = ExpenseGroupEntity::class, parentColumns = ["groupId"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = FriendEntity::class, parentColumns = ["friendId"], childColumns = ["fromFriendId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = FriendEntity::class, parentColumns = ["friendId"], childColumns = ["toFriendId"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true)
    val settlementId: Long = 0,
    val groupId: Long,
    val fromFriendId: Long, // Person paying
    val toFriendId: Long,   // Person receiving
    val amount: Double,
    val dateTime: Long
)
