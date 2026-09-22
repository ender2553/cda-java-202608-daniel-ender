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

    // INSTRUCTOR NOTE [POS1-8]: Fail-closed duplicate rejection -- a transaction id that has
    // already been processed must be rejected (return false), not silently reprocessed
    // (which would double-charge or double-count). Common mistake: using `add` and ignoring
    // its boolean return value entirely (Set.add returns false on a duplicate -- that return
    // value IS the check, students often overlook it and write a separate contains() check
    // that's fine too, but some forget to check anything and always return true).
    public boolean recordTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
        return processedTransactionIds.add(transactionId);
    }

    public boolean hasProcessed(String transactionId) {
        return processedTransactionIds.contains(transactionId);
    }

    // INSTRUCTOR NOTE [POS1-9]: Queue usage -- offer() to enqueue a cart for settlement,
    // poll() to dequeue the next one (FIFO), returning null on an empty queue rather than
    // throwing (poll's contract, vs. remove() which throws). Common mistake: using
    // `pendingSettlement.remove()` instead of `poll()` so an empty-queue settlement attempt
    // throws NoSuchElementException instead of returning null gracefully; or using a Stack/
    // LIFO structure instead of a FIFO Queue, which processes carts out of order.
    public void enqueueForSettlement(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException("cart must not be null");
        }
        pendingSettlement.offer(cart);
    }

    public Cart settleNext() {
        return pendingSettlement.poll();
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
