package academy.rti.smc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * THE ADDED CONTROL for this lab: centralized, safe exception handling.
 *
 * <p>Every error path is funneled through here and returned as an RFC 7807
 * {@link ProblemDetail} ({@code application/problem+json}). Client-facing bodies carry
 * only a generic, safe message — never a stack trace, exception class name, or internal
 * detail. Full detail is logged SERVER-SIDE via the logger, not placed in the response.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Validation failures (e.g. blank accountId) -> 400 ProblemDetail.
     *
     * <p>We surface only safe field-level messages defined on the DTO; we never echo the
     * exception type or framework internals. The full exception is logged server-side.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for request", ex);

        String safeDetail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (safeDetail.isBlank()) {
            safeDetail = "One or more fields are invalid.";
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, safeDetail);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://rti.academy/problems/validation-error"));
        return problem;
    }

    /**
     * Any unexpected server-side failure -> 500 ProblemDetail with a GENERIC message.
     *
     * <p>The real exception message (which may contain a DB connection string, secrets,
     * etc.) is logged server-side ONLY. The client receives no internal detail.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // Full detail stays on the server log, never in the response body.
        log.error("Unexpected server error handling request", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later or contact support.");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://rti.academy/problems/internal-error"));
        return problem;
    }
}
