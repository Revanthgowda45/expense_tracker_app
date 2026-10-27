package com.example.studentexpensetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String iconName;
    private String colorHex;
    private String type; // "EXPENSE", "INCOME", or "BOTH"
    private boolean isDefault;

    public CategoryEntity() {
    }

    public CategoryEntity(long id, String name, String iconName, String colorHex, String type, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.iconName = iconName;
        this.colorHex = colorHex;
        this.type = type;
        this.isDefault = isDefault;
    }

    public CategoryEntity(String name, String iconName, String colorHex, String type, boolean isDefault) {
        this(0, name, iconName, colorHex, type, isDefault);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
