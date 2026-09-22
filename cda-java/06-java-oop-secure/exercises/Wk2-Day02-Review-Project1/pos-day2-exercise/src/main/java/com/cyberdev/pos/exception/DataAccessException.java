package com.cyberdev.pos.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Day 2. Repository implementations wrap any lower-level failure from the persistence layer
 * (a real SQLException/Spring org.springframework.dao.DataAccessException from a
 * Jdbc*Repository, or an in-memory failure from an InMemory*Repository) in this exception rather than letting whatever
 * exception type the underlying store throws leak out to callers. This keeps
 * TransactionService and CheckoutService coupled to a stable, POS-specific exception type
 * instead of to persistence-layer implementation details.
 */
public class DataAccessException extends PosException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
