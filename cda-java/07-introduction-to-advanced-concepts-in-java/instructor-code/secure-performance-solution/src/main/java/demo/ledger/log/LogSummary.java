package demo.ledger.log;

import java.math.BigDecimal;

/** How many transactions a log holds, and their total amount. */
public record LogSummary(long transactions, BigDecimal total) {

    public static final LogSummary EMPTY = new LogSummary(0, BigDecimal.ZERO);

    /** The summary of one log line: a single transaction and its amount (the 5th column). */
    public static LogSummary ofLine(String line) {
        return new LogSummary(1, new BigDecimal(line.split(",", -1)[4]));
    }

    public LogSummary plus(LogSummary other) {
        return new LogSummary(transactions + other.transactions, total.add(other.total));
    }
}
