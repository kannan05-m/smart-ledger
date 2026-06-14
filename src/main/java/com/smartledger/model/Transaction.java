package com.smartledger.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public class Transaction {

    private String id;
    private LocalDate date;
    private LocalTime time;
    private String description;
    private BigDecimal amount;
    private TransactionType type;

    public Transaction() {
    }

    public Transaction(String id, LocalDate date, String description, BigDecimal amount, TransactionType type) {
        this(id, date, null, description, amount, type);
    }

    public Transaction(String id, LocalDate date, LocalTime time, String description, BigDecimal amount, TransactionType type) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.description = description;
        this.amount = amount;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal signedAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return type == TransactionType.DR ? amount.negate() : amount;
    }

    public String duplicateKey() {
        return "%s|%s|%s|%s".formatted(date, normalize(description), amount, type);
    }

    public LocalDateTime timestamp() {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, time == null ? LocalTime.NOON : time);
    }

    private String normalize(String value) {
        return Objects.toString(value, "").trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
