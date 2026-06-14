package com.smartledger.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartledger.config.ClaudeConfig;
import com.smartledger.model.AuditSummary;
import com.smartledger.model.CategorizationResult;
import com.smartledger.model.Transaction;
import com.smartledger.model.TransactionType;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class ClaudeService {

    private static final int CATEGORIZATION_BATCH_SIZE = 50;

    private final ClaudeConfig claudeConfig;
    private final RestClient claudeRestClient;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public ClaudeService(ClaudeConfig claudeConfig, RestClient claudeRestClient, ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.claudeConfig = claudeConfig;
        this.claudeRestClient = claudeRestClient;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    public Map<String, CategorizationResult> categorizeTransactions(List<Transaction> transactions) {
        Map<String, CategorizationResult> categories = new LinkedHashMap<>();
        for (int start = 0; start < transactions.size(); start += CATEGORIZATION_BATCH_SIZE) {
            List<Transaction> batch = transactions.subList(start, Math.min(start + CATEGORIZATION_BATCH_SIZE, transactions.size()));
            categories.putAll(categorizeBatch(batch));
        }
        transactions.forEach(transaction -> categories.putIfAbsent(transaction.getId(), fallbackCategory(transaction)));
        return categories;
    }

    public AuditSummary generateAuditSummary(Map<String, Object> summaryPayload) {
        if (!claudeConfig.isConfigured()) {
            return fallbackSummary(summaryPayload);
        }

        try {
            String userPrompt = "Here is the spending summary:\n\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(summaryPayload);
            String response = callClaude(loadPrompt("audit-summary.txt"), userPrompt);
            return objectMapper.readValue(response, AuditSummary.class);
        } catch (Exception ex) {
            return fallbackSummary(summaryPayload);
        }
    }

    private Map<String, CategorizationResult> categorizeBatch(List<Transaction> transactions) {
        if (!claudeConfig.isConfigured()) {
            return fallbackCategories(transactions);
        }

        try {
            List<Map<String, Object>> payload = transactions.stream()
                    .map(transaction -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", transaction.getId());
                        item.put("date", transaction.getDate());
                        item.put("time", transaction.getTime());
                        item.put("description", transaction.getDescription());
                        item.put("amount", transaction.getAmount());
                        item.put("type", transaction.getType());
                        return item;
                    })
                    .toList();

            String response = callClaude(
                    loadPrompt("categorize.txt"),
                    "Categorize these transactions:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload)
            );
            List<CategorizationResult> results = objectMapper.readValue(response, new TypeReference<>() {
            });

            Map<String, CategorizationResult> byId = new LinkedHashMap<>();
            results.forEach(result -> byId.put(result.getId(), normalizeResult(result)));
            return byId;
        } catch (Exception ex) {
            return fallbackCategories(transactions);
        }
    }

    private String callClaude(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", claudeConfig.getModel(),
                "max_tokens", claudeConfig.getMaxTokens(),
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userPrompt))
        );

        JsonNode response = claudeRestClient.post()
                .uri("/v1/messages")
                .header("x-api-key", claudeConfig.getApiKey())
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("content")) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        response.get("content").forEach(part -> {
            if (Objects.equals(part.path("type").asText(), "text")) {
                text.append(part.path("text").asText());
            }
        });
        return text.toString().trim();
    }

    private Map<String, CategorizationResult> fallbackCategories(List<Transaction> transactions) {
        Map<String, CategorizationResult> categories = new LinkedHashMap<>();
        transactions.forEach(transaction -> categories.put(transaction.getId(), fallbackCategory(transaction)));
        return categories;
    }

    private CategorizationResult fallbackCategory(Transaction transaction) {
        String description = transaction.getDescription() == null ? "" : transaction.getDescription().toLowerCase(Locale.US);
        if (transaction.getType() == TransactionType.CR) {
            return new CategorizationResult(transaction.getId(), "Income", "MEDIUM", "Credit transaction treated as income or inflow.");
        }
        if (description.contains("swiggy") || description.contains("zomato") || description.contains("restaurant") || description.contains("cafe")) {
            return new CategorizationResult(transaction.getId(), "Food & Dining", "MEDIUM", "Merchant wording suggests dining or food delivery.");
        }
        if (description.contains("grocery") || description.contains("mart") || description.contains("supermarket")) {
            return new CategorizationResult(transaction.getId(), "Groceries", "MEDIUM", "Merchant wording suggests groceries.");
        }
        if (description.contains("uber") || description.contains("ola") || description.contains("metro") || description.contains("taxi")) {
            return new CategorizationResult(transaction.getId(), "Transport", "MEDIUM", "Merchant wording suggests local transport.");
        }
        if (description.contains("fuel") || description.contains("petrol") || description.contains("diesel")) {
            return new CategorizationResult(transaction.getId(), "Fuel", "MEDIUM", "Merchant wording suggests fuel.");
        }
        if (description.contains("netflix") || description.contains("spotify") || description.contains("prime") || description.contains("subscription")) {
            return new CategorizationResult(transaction.getId(), "Subscriptions", "MEDIUM", "Merchant wording suggests a recurring subscription.");
        }
        if (description.contains("rent")) {
            return new CategorizationResult(transaction.getId(), "Rent & Housing", "MEDIUM", "Merchant wording suggests rent or housing.");
        }
        if (description.contains("electric") || description.contains("utility") || description.contains("water") || description.contains("phone")) {
            return new CategorizationResult(transaction.getId(), "Utilities", "MEDIUM", "Merchant wording suggests a utility bill.");
        }
        if (description.contains("amazon") || description.contains("flipkart") || description.contains("myntra") || description.contains("shopping")) {
            return new CategorizationResult(transaction.getId(), "Shopping", "MEDIUM", "Merchant wording suggests shopping.");
        }
        if (description.contains("atm") || description.contains("cash")) {
            return new CategorizationResult(transaction.getId(), "ATM & Cash", "MEDIUM", "Merchant wording suggests cash withdrawal.");
        }
        if (description.contains("refund")) {
            return new CategorizationResult(transaction.getId(), "Refunds", "MEDIUM", "Merchant wording suggests a refund.");
        }
        return new CategorizationResult(transaction.getId(), "Other", "LOW", "No strong merchant pattern matched locally.");
    }

    private CategorizationResult normalizeResult(CategorizationResult result) {
        if (result.getCategory() == null || result.getCategory().isBlank()) {
            result.setCategory("Other");
        }
        if (result.getConfidence() == null || result.getConfidence().isBlank()) {
            result.setConfidence("LOW");
        }
        if (result.getReason() == null || result.getReason().isBlank()) {
            result.setReason("Claude returned this category without a reason.");
        }
        return result;
    }

    private AuditSummary fallbackSummary(Map<String, Object> summaryPayload) {
        BigDecimal totalOutflow = (BigDecimal) summaryPayload.getOrDefault("totalOutflow", BigDecimal.ZERO);
        BigDecimal totalInflow = (BigDecimal) summaryPayload.getOrDefault("totalInflow", BigDecimal.ZERO);
        BigDecimal net = (BigDecimal) summaryPayload.getOrDefault("net", BigDecimal.ZERO);
        int anomalyCount = ((Number) summaryPayload.getOrDefault("anomalyCount", 0)).intValue();

        AuditSummary summary = new AuditSummary();
        summary.setHeadline("You spent ₹%s this period with a net position of ₹%s.".formatted(totalOutflow, net));
        summary.setTopFindings(new ArrayList<>(List.of(
                "Total inflow was ₹%s and total outflow was ₹%s.".formatted(totalInflow, totalOutflow),
                "%d transactions were flagged for review.".formatted(anomalyCount),
                "Review the highest-spend categories before planning next month."
        )));
        summary.setSavingsSuggestions(new ArrayList<>(List.of(
                "Reduce the largest flexible category by 10% for an immediate monthly saving.",
                "Cancel or pause unused subscriptions to reduce recurring debits."
        )));
        summary.setAnomalySummary(anomalyCount == 0
                ? "No rule-based anomalies were found."
                : "%d rule-based anomalies were found. Check duplicate, timing, and unusually large debit flags first.".formatted(anomalyCount));
        summary.setOverallScore(anomalyCount == 0 ? 82 : Math.max(45, 78 - anomalyCount * 4));
        summary.setScoreReason("Score is based on net cash flow and the number of flagged transactions.");
        return summary;
    }

    private String loadPrompt(String filename) {
        try {
            return resourceLoader.getResource("classpath:prompts/" + filename)
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "You are SmartLedger, an accurate financial audit assistant.";
        }
    }
}
