package com.cyberdev.dndrpg.exception;

/** Base type for every checked-by-design failure in this game's domain. */
public class GameException extends RuntimeException {
    public GameException(String message) { super(message); }
    public GameException(String message, Throwable cause) { super(message, cause); }
}
