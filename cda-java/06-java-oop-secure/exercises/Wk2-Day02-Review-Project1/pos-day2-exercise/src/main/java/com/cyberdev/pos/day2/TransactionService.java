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

    // TODO [POS2-5]: Store the given repository via CONSTRUCTOR INJECTION. This class must
    // NEVER instantiate its own repository (no `new SimulatedTransactionRepository(...)`
    // anywhere in here, and no no-arg constructor) -- the field type is the
    // TransactionRepository INTERFACE and the only way to set it is through this
    // constructor. Reject a null repository by throwing ValidationException
    // (com.cyberdev.pos.exception), fail closed.
    public TransactionService(TransactionRepository repository) {
        throw new UnsupportedOperationException("TODO [POS2-5]: store repository via constructor injection, reject null with ValidationException");
    }

    // TODO [POS2-6] / TODO [POS2-7]: Build a TransactionRecord from the cart's total
    // (cart.getTotal()) and save it via the injected repository (POS2-6). BEFORE saving,
    // check repository.findById(transactionId) and fail closed -- throw
    // DuplicateTransactionException (com.cyberdev.pos.exception), NOT a generic
    // IllegalStateException -- if a record with that id already exists, so a duplicate sale
    // is never recorded twice (POS2-7). Reject a null/blank cart/merchantId/transactionId
    // with ValidationException.
    public TransactionRecord recordSale(Cart cart, String merchantId, String transactionId, String memo) {
        throw new UnsupportedOperationException("TODO [POS2-6]/[POS2-7]: build+save TransactionRecord from cart total, reject duplicate transactionId with DuplicateTransactionException before saving");
    }

    public TransactionRecord recordSale(Cart cart, String merchantId) {
        return recordSale(cart, merchantId, UUID.randomUUID().toString(), "");
    }
}
