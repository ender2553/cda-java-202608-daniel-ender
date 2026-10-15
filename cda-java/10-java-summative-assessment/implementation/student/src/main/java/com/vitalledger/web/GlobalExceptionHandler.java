package com.vitalledger.web;

import com.vitalledger.exception.ResourceNotFoundException;
import com.vitalledger.exception.ValidationException;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central no-leak error handler. Every response is an RFC-7807 {@link ProblemDetail}
 * with a stable {@code code} and a {@code correlationId}. Internal detail (stack
 * traces, SQL, class names, messages from unexpected exceptions) is written ONLY to
 * the server log, keyed by the correlation id — never to the response body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex) {
        // TODO(phase D): map ValidationException to a 400 ProblemDetail (code VALIDATION_FAILED) using the caller-safe message.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleBeanValidation(MethodArgumentNotValidException ex) {
        // TODO(phase D): map bean-validation failures to a 400 ProblemDetail summarizing the first field error only.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        // TODO(phase D): return a 400 ProblemDetail (code MALFORMED_REQUEST); log internals by correlation id, never echo the parser message.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        // TODO(phase D): map ResourceNotFoundException to a 404 ProblemDetail (code NOT_FOUND).
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /**
     * Method-security ({@code @PreAuthorize}) denials surface here as
     * AccessDeniedException. Map to a no-leak 403 (never 500) so an authenticated
     * caller lacking a role gets a clean, minimal response.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        // TODO(phase D): map AccessDeniedException to a no-leak 403 ProblemDetail (code FORBIDDEN), never a 500.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        // TODO(phase D): map IllegalArgumentException to a 400 ProblemDetail (code BAD_REQUEST).
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /** Catch-all: nothing internal escapes. Full detail goes to the log only. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // TODO(phase D): return a generic 500 ProblemDetail (code INTERNAL_ERROR); full stack/detail goes only to the server log by correlation id.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    private ProblemDetail problem(HttpStatus status, String code, String title, String detail) {
        // TODO(phase D): build a ProblemDetail carrying only safe fields (title/status/detail/code/correlationId); log the correlation id server-side.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    private String logInternal(String code, Exception ex) {
        // TODO(phase D): generate a correlation id, log the full exception server-side keyed by it, and return the id (never leak it into a response body's detail).
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
