package com.cyberdev.dndrpg.exception;

/** Thrown when game reference data (from file or database) is missing,
 *  malformed, or fails validation. Fail-closed: the game must never
 *  start with partially-loaded or corrupted reference data. */
public class GameDataException extends GameException {
    public GameDataException(String message) { super(message); }
    public GameDataException(String message, Throwable cause) { super(message, cause); }
}
