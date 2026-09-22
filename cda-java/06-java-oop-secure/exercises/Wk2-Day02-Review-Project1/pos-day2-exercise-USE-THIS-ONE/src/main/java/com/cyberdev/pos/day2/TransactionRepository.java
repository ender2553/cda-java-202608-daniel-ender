package com.cyberdev.pos.day2;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TransactionRecord persistence. GIVEN infrastructure.
 * TransactionService depends on this INTERFACE, never the concrete simulated implementation
 * directly -- that is the manual dependency-injection teaching point (POS2-5).
 */
public interface TransactionRepository {
    void save(TransactionRecord record);
    Optional<TransactionRecord> findById(String transactionId);
    List<TransactionRecord> searchByMemo(String keyword);
}
