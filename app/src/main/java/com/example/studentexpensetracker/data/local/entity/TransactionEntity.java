package com.example.studentexpensetracker.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private double amount;
    private String type; // "EXPENSE" or "INCOME"
    private long categoryId;
    private String note;
    private String paymentMethod;
    private long dateTime;
    private boolean isRecurring;
    private String recurrenceType;
    private long createdAt;
    private String mood;
    private String receiptImagePath;

    public TransactionEntity() {
    }

    public TransactionEntity(
            long id,
            double amount,
            String type,
            long categoryId,
            String note,
            String paymentMethod,
            long dateTime,
            boolean isRecurring,
            String recurrenceType,
            long createdAt,
            String mood,
            String receiptImagePath) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.dateTime = dateTime;
        this.isRecurring = isRecurring;
        this.recurrenceType = recurrenceType;
        this.createdAt = createdAt;
        this.mood = mood;
        this.receiptImagePath = receiptImagePath;
    }

    public TransactionEntity(
            double amount,
            String type,
            long categoryId,
            String note,
            String paymentMethod,
            long dateTime,
            boolean isRecurring,
            String recurrenceType,
            long createdAt,
            String mood,
            String receiptImagePath) {
        this(0, amount, type, categoryId, note, paymentMethod, dateTime, isRecurring, recurrenceType, createdAt, mood, receiptImagePath);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getReceiptImagePath() {
        return receiptImagePath;
    }

    public void setReceiptImagePath(String receiptImagePath) {
        this.receiptImagePath = receiptImagePath;
    }
}
