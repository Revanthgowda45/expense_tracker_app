package com.example.studentexpensetracker.data.local.entity.split;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
    tableName = "group_members",
    primaryKeys = {"groupId", "friendId"},
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
            childColumns = "friendId",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class GroupMemberEntity {
    private long groupId;
    private long friendId;

    public GroupMemberEntity() {
    }

    public GroupMemberEntity(long groupId, long friendId) {
        this.groupId = groupId;
        this.friendId = friendId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }
}
