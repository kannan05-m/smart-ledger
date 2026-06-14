package com.smartledger.model;

public class CategorizationResult {

    private String id;
    private String category;
    private String confidence;
    private String reason;

    public CategorizationResult() {
    }

    public CategorizationResult(String id, String category, String confidence, String reason) {
        this.id = id;
        this.category = category;
        this.confidence = confidence;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
