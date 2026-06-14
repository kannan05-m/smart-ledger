package com.smartledger.util;

import com.opencsv.CSVReader;
import com.smartledger.model.Transaction;
import com.smartledger.model.TransactionType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CsvParserUtil {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d/M/uuuu").toFormatter(Locale.US),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("M/d/uuuu").toFormatter(Locale.US),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d-MMM-uuuu").toFormatter(Locale.US),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("d MMM uuuu").toFormatter(Locale.US)
    );
    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_TIME,
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("H:mm").toFormatter(Locale.US),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("H:mm:ss").toFormatter(Locale.US),
            new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern("h:mm a").toFormatter(Locale.US)
    );

    public List<Transaction> parse(MultipartFile file) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                return List.of();
            }

            Map<String, Integer> header = headerIndex(rows.getFirst());
            List<Transaction> transactions = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (isBlankRow(row)) {
                    continue;
                }
                transactions.add(parseRow(row, header, i));
            }
            return transactions;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse CSV file: " + ex.getMessage(), ex);
        }
    }

    static Transaction parseRow(String[] row, Map<String, Integer> header) {
        return parseRow(row, header, 1);
    }

    static Transaction parseRow(String[] row, Map<String, Integer> header, int rowNumber) {
        LocalDate date = parseDate(value(row, header, "date", "transaction date", "posted date"));
        LocalTime time = parseTime(value(row, header, "time", "transaction time", "timestamp", "posted time"));
        String description = value(row, header, "description", "details", "memo", "narration", "merchant");
        BigDecimal debit = parseAmount(value(row, header, "debit", "withdrawal", "dr"));
        BigDecimal credit = parseAmount(value(row, header, "credit", "deposit", "cr"));
        String amountText = value(row, header, "amount", "transaction amount");
        String typeText = value(row, header, "type", "cr/dr", "transaction type");

        TransactionType type;
        BigDecimal amount;
        if (credit.signum() > 0 || debit.signum() > 0) {
            type = credit.signum() > 0 ? TransactionType.CR : TransactionType.DR;
            amount = credit.signum() > 0 ? credit : debit;
        } else {
            BigDecimal signedAmount = parseAmount(amountText);
            type = parseType(typeText, signedAmount);
            amount = signedAmount.abs();
        }

        return new Transaction("txn_%03d".formatted(rowNumber), date, time, description, amount, type);
    }

    static Map<String, Integer> headerIndex(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            index.put(normalizeHeader(headers[i]), i);
        }
        return index;
    }

    static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing transaction date");
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Unsupported date format: " + value);
    }

    static BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        String normalized = value.trim()
                .replace(",", "")
                .replace("$", "")
                .replace("₹", "")
                .replace("(", "-")
                .replace(")", "");
        if (normalized.equals("-")) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(normalized);
    }

    static LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.contains("T")) {
            String timePart = trimmed.substring(trimmed.indexOf('T') + 1);
            if (timePart.length() >= 8) {
                timePart = timePart.substring(0, 8);
            }
            trimmed = timePart;
        }
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    static TransactionType parseType(String value, BigDecimal signedAmount) {
        if (value != null) {
            String normalized = value.trim().toUpperCase(Locale.US);
            if (normalized.contains("CR") || normalized.contains("CREDIT") || normalized.contains("DEPOSIT")) {
                return TransactionType.CR;
            }
            if (normalized.contains("DR") || normalized.contains("DEBIT") || normalized.contains("WITHDRAWAL")) {
                return TransactionType.DR;
            }
        }
        return signedAmount.signum() < 0 ? TransactionType.DR : TransactionType.CR;
    }

    private static String value(String[] row, Map<String, Integer> header, String... names) {
        for (String name : names) {
            Integer index = header.get(normalizeHeader(name));
            if (index != null && index < row.length) {
                return row[index];
            }
        }
        return "";
    }

    private static String normalizeHeader(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US).replaceAll("[^a-z0-9/ ]", "");
    }

    private static boolean isBlankRow(String[] row) {
        for (String cell : row) {
            if (cell != null && !cell.isBlank()) {
                return false;
            }
        }
        return true;
    }
}
