package demo.ledger.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** One row of {@code transactions}. */
public record Transaction(long id, String fromAccount, String toAccount, BigDecimal amount,
                          String memo, OffsetDateTime createdAt) {
}
