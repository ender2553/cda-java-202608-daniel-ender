package demo.ledger.domain;

import java.math.BigDecimal;

/** One row of {@code accounts}. */
public record Account(String accountId, BigDecimal balance) {
}
