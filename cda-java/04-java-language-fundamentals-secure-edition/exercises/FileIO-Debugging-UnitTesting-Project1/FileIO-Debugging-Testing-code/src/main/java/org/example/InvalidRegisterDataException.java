package org.example;
/**
 * Thrown when a register save file or an imported legacy scan file
 * cannot be parsed into valid findings.
 *
 * Checked (extends Exception): corrupted or tampered external data is
 * a condition the caller must decide how to recover from — it cannot
 * be prevented purely by validating earlier in the same method.
 */
public class InvalidRegisterDataException extends Exception {

    public InvalidRegisterDataException(String message) {
        super(message);
    }

    public InvalidRegisterDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
