package demo.payroll.domain;

import java.math.BigDecimal;

/** The ledger proof: opening - current must equal the sum of every stub's net. */
public record FundingCheck(BigDecimal opening, BigDecimal current, BigDecimal stubTotal, boolean balanced) {
}
