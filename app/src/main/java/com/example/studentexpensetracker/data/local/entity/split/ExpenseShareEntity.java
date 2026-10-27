package com.example.studentexpensetracker.data.local.entity.split;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
    tableName = "expense_shares",
    primaryKeys = {"sharedExpenseId", "friendId"},
    foreignKeys = {
        @ForeignKey(
            entity = SharedExpenseEntity.class,
            parentColumns = "sharedExpenseId",
            childColumns = "sharedExpenseId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = FriendEntity.class,
            parentColumns = "friendId",
            childColumns = "friendId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class ExpenseShareEntity {
    private long sharedExpenseId;
    private long friendId;
    private double owedAmount;

    public ExpenseShareEntity() {
    }

    public ExpenseShareEntity(long sharedExpenseId, long friendId, double owedAmount) {
        this.sharedExpenseId = sharedExpenseId;
        this.friendId = friendId;
        this.owedAmount = owedAmount;
    }

    public long getSharedExpenseId() {
        return sharedExpenseId;
    }

    public void setSharedExpenseId(long sharedExpenseId) {
        this.sharedExpenseId = sharedExpenseId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public double getOwedAmount() {
        return owedAmount;
    }

    public void setOwedAmount(double owedAmount) {
        this.owedAmount = owedAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpenseShareEntity)) return false;
        ExpenseShareEntity that = (ExpenseShareEntity) o;
        return sharedExpenseId == that.sharedExpenseId &&
                friendId == that.friendId &&
                Double.compare(that.owedAmount, owedAmount) == 0;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(sharedExpenseId);
        result = 31 * result + Long.hashCode(friendId);
        result = 31 * result + Double.hashCode(owedAmount);
        return result;
    }
}
