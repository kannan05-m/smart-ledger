package com.smartledger.model;

public class AnomalyFlag {

    private String type;
    private Severity severity;
    private String description;
    private String txnId;

    public AnomalyFlag() {
    }

    public AnomalyFlag(String type, Severity severity, String description, String txnId) {
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.txnId = txnId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }
}
