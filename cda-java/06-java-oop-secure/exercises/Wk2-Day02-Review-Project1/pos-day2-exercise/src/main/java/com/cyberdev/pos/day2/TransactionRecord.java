package com.cyberdev.pos.day2;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A completed (or attempted) sale transaction. Plain class, given/working infrastructure.
 */
public final class TransactionRecord {

    private final String transactionId;
    private final String merchantId;
    private final BigDecimal amount;
    private final String memo;
    private final Instant timestamp;

    public TransactionRecord(String transactionId, String merchantId, BigDecimal amount, String memo, Instant timestamp) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId must not be blank");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        this.transactionId = transactionId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.memo = memo == null ? "" : memo;
        this.timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionRecord)) return false;
        TransactionRecord that = (TransactionRecord) o;
        return transactionId.equals(that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "TransactionRecord{id='" + transactionId + "', merchant='" + merchantId
                + "', amount=" + amount + ", memo='" + memo + "'}";
    }
}
