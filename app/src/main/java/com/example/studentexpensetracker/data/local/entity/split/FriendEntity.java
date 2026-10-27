package com.example.studentexpensetracker.data.local.entity.split;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends")
public class FriendEntity {
    @PrimaryKey(autoGenerate = true)
    private long friendId;
    private String name;
    private String phoneNumber;

    public FriendEntity() {
    }

    public FriendEntity(long friendId, String name, String phoneNumber) {
        this.friendId = friendId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public FriendEntity(String name, String phoneNumber) {
        this(0, name, phoneNumber);
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
