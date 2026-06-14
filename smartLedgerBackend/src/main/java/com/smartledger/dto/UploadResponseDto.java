package com.smartledger.dto;

public class UploadResponseDto {

    private String sessionId;
    private String status;
    private int transactionCount;
    private int anomalyCount;
    private String message;

    public UploadResponseDto() {
    }

    public UploadResponseDto(String sessionId, String status, int transactionCount, int anomalyCount, String message) {
        this.sessionId = sessionId;
        this.status = status;
        this.transactionCount = transactionCount;
        this.anomalyCount = anomalyCount;
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public int getAnomalyCount() {
        return anomalyCount;
    }

    public void setAnomalyCount(int anomalyCount) {
        this.anomalyCount = anomalyCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
