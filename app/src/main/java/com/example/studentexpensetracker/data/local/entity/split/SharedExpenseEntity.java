package com.example.studentexpensetracker.data.local.entity.split;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "shared_expenses",
    foreignKeys = {
        @ForeignKey(
            entity = ExpenseGroupEntity.class,
            parentColumns = "groupId",
            childColumns = "groupId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = FriendEntity.class,
            parentColumns = "friendId",
            childColumns = "paidByFriendId",
            onDelete = ForeignKey.RESTRICT
        )
    }
)
public class SharedExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private long sharedExpenseId;
    private long groupId;
    private String description;
    private double amount;
    private long paidByFriendId;
    private long dateTime;

    public SharedExpenseEntity() {
    }

    public SharedExpenseEntity(long sharedExpenseId, long groupId, String description, double amount, long paidByFriendId, long dateTime) {
        this.sharedExpenseId = sharedExpenseId;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidByFriendId = paidByFriendId;
        this.dateTime = dateTime;
    }

    public SharedExpenseEntity(long groupId, String description, double amount, long paidByFriendId, long dateTime) {
        this(0, groupId, description, amount, paidByFriendId, dateTime);
    }

    public long getSharedExpenseId() {
        return sharedExpenseId;
    }

    public void setSharedExpenseId(long sharedExpenseId) {
        this.sharedExpenseId = sharedExpenseId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getPaidByFriendId() {
        return paidByFriendId;
    }

    public void setPaidByFriendId(long paidByFriendId) {
        this.paidByFriendId = paidByFriendId;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
