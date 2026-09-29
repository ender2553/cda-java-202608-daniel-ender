package demo.transactions;

/**
 * The only failure {@link TransferService#transfer} reports to its caller.
 *
 * <p>Its message is safe to show a user: it names the transfer, never the
 * database. The underlying {@code SQLException} (constraint names, table
 * names, SQL state) goes to the internal log instead, and is kept as the
 * cause only for code that needs to diagnose the failure.
 */
public final class TransferFailedException extends RuntimeException {

    public TransferFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
