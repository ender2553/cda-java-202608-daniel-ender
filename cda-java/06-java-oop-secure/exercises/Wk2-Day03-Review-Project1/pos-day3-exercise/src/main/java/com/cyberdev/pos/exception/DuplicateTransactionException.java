package com.cyberdev.pos.exception;

/**
 * Day 2. Thrown by TransactionService.recordSale (POS2-7) when a transaction id that has
 * already been recorded is submitted again -- the fail-closed duplicate-rejection behavior,
 * now expressed as a specific, catchable exception type instead of a generic
 * IllegalStateException.
 */
public class DuplicateTransactionException extends PosException {

    public DuplicateTransactionException(String transactionId) {
        super("Duplicate transactionId, already recorded: " + transactionId);
    }
}
