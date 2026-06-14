package com.smartledger.service;

import com.smartledger.model.AnomalyFlag;
import com.smartledger.model.CategorizedTransaction;
import com.smartledger.model.Severity;
import com.smartledger.model.Transaction;
import com.smartledger.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnomalyService {

    private static final BigDecimal DUPLICATE_TOLERANCE = BigDecimal.ONE;
    private static final BigDecimal SMALL_SUBSCRIPTION_MIN = new BigDecimal("99");
    private static final BigDecimal SMALL_SUBSCRIPTION_MAX = new BigDecimal("999");

    public List<AnomalyFlag> detectBeforeCategorization(List<Transaction> transactions) {
        List<AnomalyFlag> flags = new ArrayList<>();
        flags.addAll(validateBasics(transactions));
        flags.addAll(detectDuplicates(transactions));
        flags.addAll(detectLateNightCharges(transactions));
        flags.addAll(detectWeekendSpike(transactions));
        return flags;
    }

    public List<AnomalyFlag> detectAfterCategorization(List<CategorizedTransaction> transactions) {
        List<AnomalyFlag> flags = new ArrayList<>();
        flags.addAll(detectLargeCategoryTransactions(transactions));
        flags.addAll(detectSubscriptionCreep(transactions));
        return flags;
    }

    private List<AnomalyFlag> validateBasics(List<Transaction> transactions) {
        List<AnomalyFlag> flags = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getDate() != null && transaction.getDate().isAfter(LocalDate.now())) {
                flags.add(new AnomalyFlag("FUTURE_DATE", Severity.HIGH, "Transaction date is in the future.", transaction.getId()));
            }
            if (transaction.getAmount() == null || transaction.getAmount().signum() <= 0) {
                flags.add(new AnomalyFlag("INVALID_AMOUNT", Severity.HIGH, "Transaction amount should be greater than zero.", transaction.getId()));
            }
        }
        return flags;
    }

    private List<AnomalyFlag> detectDuplicates(List<Transaction> transactions) {
        List<AnomalyFlag> flags = new ArrayList<>();
        Set<String> flaggedPairs = new HashSet<>();
        List<Transaction> debits = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .sorted(Comparator.comparing(Transaction::timestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        for (int i = 0; i < debits.size(); i++) {
            Transaction current = debits.get(i);
            for (int j = i + 1; j < debits.size(); j++) {
                Transaction candidate = debits.get(j);
                if (isDuplicate(current, candidate)) {
                    addDuplicateFlag(flags, flaggedPairs, current);
                    addDuplicateFlag(flags, flaggedPairs, candidate);
                }
            }
        }
        return flags;
    }

    private boolean isDuplicate(Transaction first, Transaction second) {
        if (first.getAmount() == null || second.getAmount() == null || first.timestamp() == null || second.timestamp() == null) {
            return false;
        }
        BigDecimal difference = first.getAmount().subtract(second.getAmount()).abs();
        long hours = Math.abs(Duration.between(first.timestamp(), second.timestamp()).toHours());
        String firstMerchant = merchantKeyword(first.getDescription());
        String secondMerchant = merchantKeyword(second.getDescription());
        return difference.compareTo(DUPLICATE_TOLERANCE) <= 0
                && hours <= 48
                && !firstMerchant.isBlank()
                && firstMerchant.equals(secondMerchant);
    }

    private void addDuplicateFlag(List<AnomalyFlag> flags, Set<String> flaggedIds, Transaction transaction) {
        if (flaggedIds.add(transaction.getId())) {
            flags.add(new AnomalyFlag(
                    "POSSIBLE_DUPLICATE",
                    Severity.MEDIUM,
                    "Same merchant keyword and nearly same amount appears within 48 hours.",
                    transaction.getId()
            ));
        }
    }

    private List<AnomalyFlag> detectLateNightCharges(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .filter(transaction -> transaction.getTime() != null)
                .filter(transaction -> !transaction.getTime().isBefore(LocalTime.MIDNIGHT) && transaction.getTime().isBefore(LocalTime.of(5, 0)))
                .map(transaction -> new AnomalyFlag("LATE_NIGHT_CHARGE", Severity.MEDIUM, "Debit occurred between midnight and 5 AM.", transaction.getId()))
                .toList();
    }

    private List<AnomalyFlag> detectWeekendSpike(List<Transaction> transactions) {
        Map<LocalDate, BigDecimal> weekdayTotals = new HashMap<>();
        Map<LocalDate, BigDecimal> weekendTotals = new HashMap<>();
        transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .filter(transaction -> transaction.getDate() != null && transaction.getAmount() != null)
                .forEach(transaction -> {
                    Map<LocalDate, BigDecimal> target = isWeekend(transaction.getDate()) ? weekendTotals : weekdayTotals;
                    target.merge(transaction.getDate(), transaction.getAmount(), BigDecimal::add);
                });

        if (weekdayTotals.isEmpty() || weekendTotals.isEmpty()) {
            return List.of();
        }

        BigDecimal weekdayAverage = weekdayTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(weekdayTotals.size()), 2, RoundingMode.HALF_UP);

        List<AnomalyFlag> flags = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : weekendTotals.entrySet()) {
            if (entry.getValue().compareTo(weekdayAverage.multiply(BigDecimal.valueOf(2))) > 0) {
                transactions.stream()
                        .filter(transaction -> entry.getKey().equals(transaction.getDate()))
                        .filter(transaction -> transaction.getType() == TransactionType.DR)
                        .forEach(transaction -> flags.add(new AnomalyFlag(
                                "WEEKEND_SPIKE",
                                Severity.LOW,
                                "Weekend daily debit total is more than 2x the weekday daily average.",
                                transaction.getId()
                        )));
            }
        }
        return flags;
    }

    private List<AnomalyFlag> detectLargeCategoryTransactions(List<CategorizedTransaction> transactions) {
        Map<String, BigDecimal> categoryAverage = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .filter(transaction -> transaction.getAmount() != null)
                .collect(Collectors.groupingBy(
                        CategorizedTransaction::getCategory,
                        Collectors.collectingAndThen(Collectors.toList(), this::averageAmount)
                ));

        List<AnomalyFlag> flags = new ArrayList<>();
        for (CategorizedTransaction transaction : transactions) {
            if (transaction.getType() != TransactionType.DR || transaction.getAmount() == null) {
                continue;
            }
            BigDecimal average = categoryAverage.get(transaction.getCategory());
            if (average != null && average.signum() > 0 && transaction.getAmount().compareTo(average.multiply(BigDecimal.valueOf(2))) > 0) {
                flags.add(new AnomalyFlag(
                        "LARGE_CATEGORY_TRANSACTION",
                        Severity.MEDIUM,
                        "Single debit is more than 2x the average for category '%s'.".formatted(transaction.getCategory()),
                        transaction.getId()
                ));
            }
        }
        return flags;
    }

    private BigDecimal averageAmount(List<CategorizedTransaction> transactions) {
        return transactions.stream()
                .map(CategorizedTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private List<AnomalyFlag> detectSubscriptionCreep(List<CategorizedTransaction> transactions) {
        List<CategorizedTransaction> subscriptions = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.DR)
                .filter(transaction -> "Subscriptions".equalsIgnoreCase(transaction.getCategory()))
                .filter(transaction -> transaction.getAmount() != null)
                .filter(transaction -> transaction.getAmount().compareTo(SMALL_SUBSCRIPTION_MIN) >= 0)
                .filter(transaction -> transaction.getAmount().compareTo(SMALL_SUBSCRIPTION_MAX) <= 0)
                .toList();

        if (subscriptions.size() < 5) {
            return List.of();
        }

        return subscriptions.stream()
                .map(transaction -> new AnomalyFlag(
                        "SUBSCRIPTION_CREEP",
                        Severity.LOW,
                        "Five or more recurring-sized subscription debits were found this period.",
                        transaction.getId()
                ))
                .toList();
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private String merchantKeyword(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }
        String cleaned = description.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\b(order|txn|upi|neft|imps|payment|paid|to|from)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isBlank()) {
            return "";
        }
        return cleaned.split(" ")[0];
    }
}
