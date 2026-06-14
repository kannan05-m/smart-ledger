package com.smartledger.model;

import java.util.ArrayList;
import java.util.List;

public class AuditSummary {

    private String headline;
    private List<String> topFindings = new ArrayList<>();
    private List<String> savingsSuggestions = new ArrayList<>();
    private String anomalySummary;
    private int overallScore;
    private String scoreReason;

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public List<String> getTopFindings() {
        return topFindings;
    }

    public void setTopFindings(List<String> topFindings) {
        this.topFindings = topFindings;
    }

    public List<String> getSavingsSuggestions() {
        return savingsSuggestions;
    }

    public void setSavingsSuggestions(List<String> savingsSuggestions) {
        this.savingsSuggestions = savingsSuggestions;
    }

    public String getAnomalySummary() {
        return anomalySummary;
    }

    public void setAnomalySummary(String anomalySummary) {
        this.anomalySummary = anomalySummary;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(int overallScore) {
        this.overallScore = overallScore;
    }

    public String getScoreReason() {
        return scoreReason;
    }

    public void setScoreReason(String scoreReason) {
        this.scoreReason = scoreReason;
    }
}
