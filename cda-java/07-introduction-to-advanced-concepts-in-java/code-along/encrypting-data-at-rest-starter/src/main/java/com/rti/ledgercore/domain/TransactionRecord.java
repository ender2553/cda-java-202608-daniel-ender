package com.rti.ledgercore.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * A single LedgerCore transaction-log line: {@code fromAccountId,toAccountId,amount,memo,timestamp}
 * (ISO-8601 instant). {@link #parse(String)} never throws on a malformed line — it returns a
 * sentinel record for which {@link #isValid()} is {@code false}.
 */
public record TransactionRecord(
        String fromAccountId,
        String toAccountId,
        BigDecimal amount,
        String memo,
        Instant timestamp) {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

    public static TransactionRecord parse(String line) {
        if (line == null) {
            return invalid();
        }
        try {
            String[] parts = line.split(",", -1);
            if (parts.length != 5) {
                return invalid();
            }
            String fromAccountId = parts[0].trim();
            String toAccountId = parts[1].trim();
            BigDecimal amount = new BigDecimal(parts[2].trim());
            String memo = parts[3].trim();
            Instant timestamp = Instant.parse(parts[4].trim());
            return new TransactionRecord(fromAccountId, toAccountId, amount, memo, timestamp);
        } catch (RuntimeException malformedLine) {
            return invalid();
        }
    }

    private static TransactionRecord invalid() {
        return new TransactionRecord("", "", null, "", null);
    }

    public boolean isValid() {
        return fromAccountId != null && !fromAccountId.isBlank()
                && toAccountId != null && !toAccountId.isBlank()
                && amount != null && amount.signum() > 0
                && timestamp != null;
    }

    /** Renders this record back to the same CSV line shape {@link #parse(String)} accepts. */
    public String toExportRow() {
        return fromAccountId + "," + toAccountId + "," + amount + "," + memo + "," + TIMESTAMP_FORMAT.format(timestamp);
    }
}
