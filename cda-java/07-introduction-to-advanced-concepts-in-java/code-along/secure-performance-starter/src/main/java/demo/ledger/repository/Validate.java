package demo.ledger.repository;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Boundary checks shared by every repository and the log exporter. They run
 * before any SQL or file access does, on every code path, no matter which
 * caller forgot to check upstream.
 * Messages describe the caller's own input only, so they are safe to show.
 */
public final class Validate {

    private static final Pattern ACCOUNT_ID = Pattern.compile("ACC-\\d{8}");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000000.00");

    private Validate() {
    }

    public static String accountId(String value) {
        if (value == null || !ACCOUNT_ID.matcher(value).matches()) {
            throw new IllegalArgumentException("Account ID must look like ACC-00000001");
        }
        return value;
    }

    /** Parameterized SQL will happily run "-50.00"; only a boundary check rejects it. */
    public static BigDecimal amount(BigDecimal value) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (value.scale() > 2 || value.compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("Amount must have at most 2 decimals and be no more than " + MAX_AMOUNT);
        }
        return value;
    }

    public static String matches(String value, Pattern pattern, String message) {
        if (value == null || !pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static String maxLength(String value, int max, String field) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(field + " must be " + max + " characters or fewer");
        }
        return value;
    }

    /** A caller-chosen size is rejected when out of range, never quietly truncated. */
    public static int between(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
