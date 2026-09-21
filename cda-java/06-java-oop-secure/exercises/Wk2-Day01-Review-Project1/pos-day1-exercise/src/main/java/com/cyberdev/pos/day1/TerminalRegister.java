package com.cyberdev.pos.day1;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A single-terminal register. Demonstrates Set (duplicate rejection), Queue (pending
 * settlement batch), and Map (running per-merchant daily totals) usage.
 */
public final class TerminalRegister {

    private final Set<String> processedTransactionIds = new HashSet<>();
    private final Queue<Cart> pendingSettlement = new ArrayDeque<>();
    private final Map<String, BigDecimal> terminalDailyTotals = new HashMap<>();

    // TODO [POS1-8]: Record the transactionId as processed. Return true if this is the
    // first time it has been seen, or false if it is a duplicate (fail closed -- do not
    // reprocess a duplicate transaction id). Hint: Set.add(...) already returns exactly
    // this boolean.
    public boolean recordTransactionId(String transactionId) {
        throw new UnsupportedOperationException("TODO [POS1-8]: use the Set to reject duplicate transaction ids (fail closed)");
    }

    public boolean hasProcessed(String transactionId) {
        return processedTransactionIds.contains(transactionId);
    }

    // TODO [POS1-9]: Enqueue the cart for settlement (FIFO).
    public void enqueueForSettlement(Cart cart) {
        throw new UnsupportedOperationException("TODO [POS1-9]: enqueue cart onto the pending settlement Queue");
    }

    // TODO [POS1-9]: Dequeue and return the next cart for settlement (FIFO). Must return
    // null (not throw) when the queue is empty.
    public Cart settleNext() {
        throw new UnsupportedOperationException("TODO [POS1-9]: dequeue next cart for settlement, return null when empty");
    }

    public int pendingSettlementCount() {
        return pendingSettlement.size();
    }

    public void addToDailyTotal(String merchantId, BigDecimal amount) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId must not be blank");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        terminalDailyTotals.merge(merchantId, amount, BigDecimal::add);
    }

    public BigDecimal getDailyTotal(String merchantId) {
        return terminalDailyTotals.getOrDefault(merchantId, BigDecimal.ZERO);
    }
}
