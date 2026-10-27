package com.example.studentexpensetracker.data.local.entity.split;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expense_groups")
public class ExpenseGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long groupId;
    private String name;
    private String description;
    private long createdAt;

    public ExpenseGroupEntity() {
    }

    public ExpenseGroupEntity(long groupId, String name, String description, long createdAt) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public ExpenseGroupEntity(String name, String description, long createdAt) {
        this(0, name, description, createdAt);
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
