package com.smartledger.service;

import com.smartledger.dto.AuditReportDto;
import com.smartledger.model.AnomalyFlag;
import com.smartledger.model.AuditReport;
import com.smartledger.model.AuditStatus;
import com.smartledger.model.CategorizationResult;
import com.smartledger.model.CategorizedTransaction;
import com.smartledger.model.Transaction;
import com.smartledger.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AnomalyService anomalyService;
    private final ClaudeService claudeService;
    private final Map<String, AuditReport> reports = new ConcurrentHashMap<>();

    public AuditService(AnomalyService anomalyService, ClaudeService claudeService) {
        this.anomalyService = anomalyService;
        this.claudeService = claudeService;
    }

    public AuditReport startAudit(List<Transaction> transactions) {
        String sessionId = UUID.randomUUID().toString();
        AuditReport report = new AuditReport();
        report.setSessionId(sessionId);
        report.setStatus(AuditStatus.PROCESSING);
        reports.put(sessionId, report);

        CompletableFuture.runAsync(() -> processAudit(sessionId, transactions));
        return report;
    }

    public Optional<AuditReport> findReport(String sessionId) {
        return Optional.ofNullable(reports.get(sessionId));
    }

    public AuditReportDto toDto(AuditReport report) {
        AuditReportDto dto = new AuditReportDto();
        dto.setSessionId(report.getSessionId());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setStatus(report.getStatus());
        dto.setErrorMessage(report.getErrorMessage());
        dto.setSummary(report.getSummary());
        dto.setTotalCredits(report.getTotalCredits());
        dto.setTotalDebits(report.getTotalDebits());
        dto.setNetAmount(report.getNetAmount());
        dto.setCategoryTotals(report.getCategoryTotals());
        dto.setAnomalies(report.getAnomalies());
        dto.setTransactions(report.getTransactions());
        return dto;
    }

    private void processAudit(String sessionId, List<Transaction> transactions) {
        AuditReport report = reports.get(sessionId);
        if (report == null) {
            return;
        }

        try {
            List<AnomalyFlag> anomalies = new ArrayList<>(anomalyService.detectBeforeCategorization(transactions));
            Map<String, CategorizationResult> categories = claudeService.categorizeTransactions(transactions);

            List<CategorizedTransaction> categorized = categorize(transactions, categories);
            anomalies.addAll(anomalyService.detectAfterCategorization(categorized));
            Map<String, List<AnomalyFlag>> flagsByTransaction = anomalies.stream()
                    .collect(Collectors.groupingBy(AnomalyFlag::getTxnId));
            categorized.forEach(transaction -> transaction.setFlags(flagsByTransaction.getOrDefault(transaction.getId(), List.of())));

            report.setTransactions(categorized);
            report.setAnomalies(anomalies);
            report.setTotalCredits(totalByType(transactions, TransactionType.CR));
            report.setTotalDebits(totalByType(transactions, TransactionType.DR));
            report.setNetAmount(report.getTotalCredits().subtract(report.getTotalDebits()));
            report.setCategoryTotals(categoryTotals(categorized));
            report.setSummary(claudeService.generateAuditSummary(summaryPayload(report)));
            report.setStatus(AuditStatus.READY);
        } catch (Exception ex) {
            report.setStatus(AuditStatus.FAILED);
            report.setErrorMessage(ex.getMessage());
        }
    }

    private List<CategorizedTransaction> categorize(List<Transaction> transactions, Map<String, CategorizationResult> categories) {
        List<CategorizedTransaction> categorized = new ArrayList<>();
        for (Transaction transaction : transactions) {
            CategorizationResult result = categories.getOrDefault(
                    transaction.getId(),
                    new CategorizationResult(transaction.getId(), "Other", "LOW", "No category was returned.")
            );
            CategorizedTransaction categorizedTransaction = new CategorizedTransaction(transaction, result.getCategory());
            categorizedTransaction.setCategoryConfidence(result.getConfidence());
            categorizedTransaction.setCategoryReason(result.getReason());
            categorized.add(categorizedTransaction);
        }
        return categorized;
    }

    private Map<String, Object> summaryPayload(AuditReport report) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalTransactions", report.getTransactions().size());
        payload.put("totalInflow", report.getTotalCredits());
        payload.put("totalOutflow", report.getTotalDebits());
        payload.put("net", report.getNetAmount());
        payload.put("spendingByCategory", categoryBreakdown(report));
        payload.put("anomalyCount", report.getAnomalies().size());
        payload.put("anomaliesDetected", anomalyBreakdown(report.getAnomalies()));
        payload.put("topMerchantsBySpend", topMerchants(report.getTransactions()));
        payload.put("monthOverMonthTrend", monthOverMonthTrend(report.getTransactions()));
        return payload;
    }

    private BigDecimal totalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> categoryTotals(List<CategorizedTransaction> transactions) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .forEach(transaction -> totals.merge(transaction.getCategory(), transaction.getAmount(), BigDecimal::add));
        return totals;
    }

    private List<Map<String, Object>> categoryBreakdown(AuditReport report) {
        BigDecimal outflow = report.getTotalDebits();
        return report.getCategoryTotals().entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("category", entry.getKey());
                    row.put("amount", entry.getValue());
                    row.put("percent", outflow.signum() == 0
                            ? BigDecimal.ZERO
                            : entry.getValue().multiply(BigDecimal.valueOf(100)).divide(outflow, 2, RoundingMode.HALF_UP));
                    return row;
                })
                .toList();
    }

    private Map<String, Long> anomalyBreakdown(List<AnomalyFlag> anomalies) {
        return anomalies.stream()
                .collect(Collectors.groupingBy(AnomalyFlag::getType, LinkedHashMap::new, Collectors.counting()));
    }

    private List<Map<String, Object>> topMerchants(List<CategorizedTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .filter(transaction -> transaction.getAmount() != null)
                .collect(Collectors.groupingBy(
                        transaction -> merchantLabel(transaction.getDescription()),
                        Collectors.reducing(BigDecimal.ZERO, CategorizedTransaction::getAmount, BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("merchant", entry.getKey());
                    row.put("amount", entry.getValue());
                    return row;
                })
                .toList();
    }

    private List<Map<String, Object>> monthOverMonthTrend(List<CategorizedTransaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getDate() != null)
                .filter(transaction -> transaction.getAmount() != null)
                .collect(Collectors.groupingBy(
                        transaction -> YearMonth.from(transaction.getDate()).toString(),
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, transaction -> transaction.getType() == TransactionType.DR
                                ? transaction.getAmount()
                                : transaction.getAmount().negate(), BigDecimal::add)
                ))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("month", entry.getKey());
                    row.put("netOutflow", entry.getValue());
                    return row;
                })
                .toList();
    }

    private String merchantLabel(String description) {
        if (description == null || description.isBlank()) {
            return "Unknown";
        }
        String cleaned = description.replaceAll("[^A-Za-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        String[] parts = cleaned.split(" ");
        return parts.length == 0 ? "Unknown" : parts[0];
    }
}
