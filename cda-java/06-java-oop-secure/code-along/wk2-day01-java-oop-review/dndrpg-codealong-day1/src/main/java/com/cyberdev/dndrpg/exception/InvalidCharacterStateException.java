package com.cyberdev.dndrpg.exception;

/** Thrown when a mutation would push a character/monster into an
 *  impossible state (negative damage, negative heal, etc.). */
public class InvalidCharacterStateException extends GameException {
    public InvalidCharacterStateException(String message) { super(message); }
}
