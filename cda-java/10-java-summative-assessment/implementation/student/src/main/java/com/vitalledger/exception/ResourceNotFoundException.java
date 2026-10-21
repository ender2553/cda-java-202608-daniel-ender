package com.vitalledger.exception;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404 with a generic,
 * non-enumerating message (does not confirm which internal id space was probed).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String safeMessage) {
        super(safeMessage);
    }
}
