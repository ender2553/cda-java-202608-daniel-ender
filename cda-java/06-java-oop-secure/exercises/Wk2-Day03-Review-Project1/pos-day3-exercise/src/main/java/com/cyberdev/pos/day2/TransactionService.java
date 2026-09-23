package com.cyberdev.pos.day2;

import com.cyberdev.pos.day1.Cart;
import com.cyberdev.pos.exception.DuplicateTransactionException;
import com.cyberdev.pos.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

/**
 * Records sales as TransactionRecords. Depends on TransactionRepository via constructor
 * injection -- manual IoC, no framework container.
 */
public final class TransactionService {

    private final TransactionRepository repository;

    // NOTE [POS2-5]: The field type is the TransactionRepository INTERFACE, and
    // the only way to set it is through the constructor -- TransactionService never calls
    // `new JdbcTransactionRepository(...)`/`new InMemoryTransactionRepository(...)` itself. This is the manual-dependency-
    // injection lesson: the service is decoupled from which repository implementation it
    // gets, so a test can hand it a fake/in-memory repository and swap the real one in
    // production without touching TransactionService's code. Common mistake: adding a
    // no-arg constructor that instantiates a concrete TransactionRepository internally
    // "for convenience" -- that silently defeats the whole point even if the graded
    // constructor-injection test still passes.
    public TransactionService(TransactionRepository repository) {
        if (repository == null) {
            throw new ValidationException("repository must not be null");
        }
        this.repository = repository;
    }

    // NOTE [POS2-6]: Builds a TransactionRecord from the Day 1 Cart's total and
    // saves it via the injected repository. Common mistake: recomputing the total by hand
    // instead of calling cart.getTotal() (drifts if LineItem logic changes), or saving
    // before generating/validating the transaction id.
    //
    // NOTE [POS2-7]: Fail-closed duplicate rejection -- recordSale must check
    // repository.findById(transactionId) BEFORE saving and refuse (throw, per the contract
    // below) if that id already exists, rather than saving a second time and silently
    // double-counting the sale. As of the custom-exception-hierarchy addition, this throws
    // the specific DuplicateTransactionException (com.cyberdev.pos.exception) rather than a
    // generic IllegalStateException, so callers can catch this exact failure mode. Common
    // mistake: checking for the duplicate AFTER calling save() (too late -- the bad write
    // already happened), not checking at all, or throwing a generic RuntimeException/
    // IllegalStateException instead of the specific DuplicateTransactionException type.
    public TransactionRecord recordSale(Cart cart, String merchantId, String transactionId, String memo) {
        if (cart == null) {
            throw new ValidationException("cart must not be null");
        }
        if (merchantId == null || merchantId.isBlank()) {
            throw new ValidationException("merchantId must not be blank");
        }
        if (transactionId == null || transactionId.isBlank()) {
            throw new ValidationException("transactionId must not be blank");
        }
        if (repository.findById(transactionId).isPresent()) {
            throw new DuplicateTransactionException(transactionId);
        }
        TransactionRecord record = new TransactionRecord(transactionId, merchantId, cart.getTotal(), memo, Instant.now());
        repository.save(record);
        return record;
    }

    public TransactionRecord recordSale(Cart cart, String merchantId) {
        return recordSale(cart, merchantId, UUID.randomUUID().toString(), "");
    }
}
