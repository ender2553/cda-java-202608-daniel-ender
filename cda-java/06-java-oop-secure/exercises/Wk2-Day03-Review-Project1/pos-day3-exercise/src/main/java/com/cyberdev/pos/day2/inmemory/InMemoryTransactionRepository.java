package com.cyberdev.pos.day2.inmemory;

import com.cyberdev.pos.day2.TransactionRecord;
import com.cyberdev.pos.day2.TransactionRepository;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * An in-memory stand-in for TransactionRepository so the exercise (and Day 4's console demo)
 * runs without any database setup. JdbcTransactionRepository (repository/jdbc) satisfies the
 * exact same TransactionRepository interface -- swapping one for the other in Main.java is
 * the whole point of coding to an interface instead of a concrete class.
 *
 * This replaces the old SimulatedTransactionRepository/SimulatedDatabase pair: same behavior,
 * just a plain Map instead of a hand-rolled fake "database" now that a REAL database-backed
 * implementation (JdbcTransactionRepository) exists alongside it.
 */
public final class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<String, TransactionRecord> byId = new ConcurrentHashMap<>();

    @Override
    public void save(TransactionRecord record) {
        if (record == null) {
            throw new ValidationException("record must not be null");
        }
        try {
            byId.put(record.getTransactionId(), record);
        } catch (RuntimeException e) {
            throw new DataAccessException("Failed to save transaction " + record.getTransactionId(), e);
        }
    }

    @Override
    public Optional<TransactionRecord> findById(String transactionId) {
        if (transactionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byId.get(transactionId));
    }

    @Override
    public List<TransactionRecord> searchByMemo(String keyword) {
        if (keyword == null) {
            return List.of();
        }
        List<TransactionRecord> results = new ArrayList<>();
        for (TransactionRecord record : byId.values()) {
            if (record.getMemo() != null && record.getMemo().contains(keyword)) {
                results.add(record);
            }
        }
        return results;
    }
}
