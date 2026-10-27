package com.example.studentexpensetracker.domain.scanner;

public class ParsedReceipt {
    private final Double amount;
    private final String merchantName;
    private final Long dateMillis;
    private final String suggestedCategory;
    private final Double taxAmount;
    private final String rawText;
    private final String receiptImagePath;

    public ParsedReceipt(
            Double amount,
            String merchantName,
            Long dateMillis,
            String suggestedCategory,
            Double taxAmount,
            String rawText,
            String receiptImagePath) {
        this.amount = amount;
        this.merchantName = merchantName;
        this.dateMillis = dateMillis;
        this.suggestedCategory = suggestedCategory;
        this.taxAmount = taxAmount;
        this.rawText = rawText != null ? rawText : "";
        this.receiptImagePath = receiptImagePath;
    }

    public Double getAmount() {
        return amount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public Long getDateMillis() {
        return dateMillis;
    }

    public String getSuggestedCategory() {
        return suggestedCategory;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public String getRawText() {
        return rawText;
    }

    public String getReceiptImagePath() {
        return receiptImagePath;
    }
}
