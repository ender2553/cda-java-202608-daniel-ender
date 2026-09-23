package com.cyberdev.dndrpg.exception;

/** Wraps a lower-level persistence failure (SQLException, IOException).
 *  Repositories throw this instead of leaking SQLException up through
 *  layers that have no business knowing SQL exists. */
public class DataAccessException extends GameException {
    public DataAccessException(String message, Throwable cause) { super(message, cause); }
}
