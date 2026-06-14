package com.smartledger.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CategorizedTransaction extends Transaction {

    private String category;
    private String categoryConfidence;
    private String categoryReason;
    private boolean flagged;
    private List<AnomalyFlag> flags = new ArrayList<>();

    public CategorizedTransaction() {
    }

    public CategorizedTransaction(Transaction transaction, String category) {
        super(transaction.getId(), transaction.getDate(), transaction.getTime(), transaction.getDescription(), transaction.getAmount(), transaction.getType());
        this.category = category;
    }

    public CategorizedTransaction(String id, LocalDate date, String description, BigDecimal amount, TransactionType type, String category) {
        super(id, date, description, amount, type);
        this.category = category;
    }

    public CategorizedTransaction(String id, LocalDate date, LocalTime time, String description, BigDecimal amount, TransactionType type, String category) {
        super(id, date, time, description, amount, type);
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryConfidence() {
        return categoryConfidence;
    }

    public void setCategoryConfidence(String categoryConfidence) {
        this.categoryConfidence = categoryConfidence;
    }

    public String getCategoryReason() {
        return categoryReason;
    }

    public void setCategoryReason(String categoryReason) {
        this.categoryReason = categoryReason;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public List<AnomalyFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<AnomalyFlag> flags) {
        this.flags = flags;
        this.flagged = flags != null && !flags.isEmpty();
    }
}
