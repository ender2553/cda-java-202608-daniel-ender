package org.example;

public class InvalidLogMessageException extends RuntimeException {
    public InvalidLogMessageException(String message) {
        super(message);
    }
}
