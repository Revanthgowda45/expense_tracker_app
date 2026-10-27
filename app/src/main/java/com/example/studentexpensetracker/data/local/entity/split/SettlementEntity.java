package com.example.studentexpensetracker.data.local.entity.split;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "settlements",
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
            childColumns = "fromFriendId",
            onDelete = ForeignKey.RESTRICT
        ),
        @ForeignKey(
            entity = FriendEntity.class,
            parentColumns = "friendId",
            childColumns = "toFriendId",
            onDelete = ForeignKey.RESTRICT
        )
    }
)
public class SettlementEntity {
    @PrimaryKey(autoGenerate = true)
    private long settlementId;
    private long groupId;
    private long fromFriendId; // Person paying
    private long toFriendId;   // Person receiving
    private double amount;
    private long dateTime;

    public SettlementEntity() {
    }

    public SettlementEntity(long settlementId, long groupId, long fromFriendId, long toFriendId, double amount, long dateTime) {
        this.settlementId = settlementId;
        this.groupId = groupId;
        this.fromFriendId = fromFriendId;
        this.toFriendId = toFriendId;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public SettlementEntity(long groupId, long fromFriendId, long toFriendId, double amount, long dateTime) {
        this(0, groupId, fromFriendId, toFriendId, amount, dateTime);
    }

    public long getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(long settlementId) {
        this.settlementId = settlementId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getFromFriendId() {
        return fromFriendId;
    }

    public void setFromFriendId(long fromFriendId) {
        this.fromFriendId = fromFriendId;
    }

    public long getToFriendId() {
        return toFriendId;
    }

    public void setToFriendId(long toFriendId) {
        this.toFriendId = toFriendId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
