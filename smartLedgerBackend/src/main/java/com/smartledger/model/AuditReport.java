package com.smartledger.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuditReport {

    private String sessionId;
    private Instant createdAt = Instant.now();
    private AuditStatus status = AuditStatus.PROCESSING;
    private String errorMessage;
    private AuditSummary summary;
    private BigDecimal totalCredits = BigDecimal.ZERO;
    private BigDecimal totalDebits = BigDecimal.ZERO;
    private BigDecimal netAmount = BigDecimal.ZERO;
    private List<CategorizedTransaction> transactions = new ArrayList<>();
    private List<AnomalyFlag> anomalies = new ArrayList<>();
    private Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public void setStatus(AuditStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public AuditSummary getSummary() {
        return summary;
    }

    public void setSummary(AuditSummary summary) {
        this.summary = summary;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public void setTotalDebits(BigDecimal totalDebits) {
        this.totalDebits = totalDebits;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public List<CategorizedTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<CategorizedTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<AnomalyFlag> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<AnomalyFlag> anomalies) {
        this.anomalies = anomalies;
    }

    public Map<String, BigDecimal> getCategoryTotals() {
        return categoryTotals;
    }

    public void setCategoryTotals(Map<String, BigDecimal> categoryTotals) {
        this.categoryTotals = categoryTotals;
    }
}
