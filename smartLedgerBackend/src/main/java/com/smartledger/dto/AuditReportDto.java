package com.smartledger.dto;

import com.smartledger.model.AnomalyFlag;
import com.smartledger.model.AuditStatus;
import com.smartledger.model.AuditSummary;
import com.smartledger.model.CategorizedTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AuditReportDto {

    private String sessionId;
    private Instant createdAt;
    private AuditStatus status;
    private String errorMessage;
    private AuditSummary summary;
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    private BigDecimal netAmount;
    private Map<String, BigDecimal> categoryTotals;
    private List<AnomalyFlag> anomalies;
    private List<CategorizedTransaction> transactions;

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

    public Map<String, BigDecimal> getCategoryTotals() {
        return categoryTotals;
    }

    public void setCategoryTotals(Map<String, BigDecimal> categoryTotals) {
        this.categoryTotals = categoryTotals;
    }

    public List<AnomalyFlag> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<AnomalyFlag> anomalies) {
        this.anomalies = anomalies;
    }

    public List<CategorizedTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<CategorizedTransaction> transactions) {
        this.transactions = transactions;
    }
}
