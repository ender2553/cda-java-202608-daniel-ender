package com.rti.ledgercore.domain;

import java.math.BigDecimal;

/** A LedgerCore account balance record. */
public record Account(String accountId, BigDecimal balance) {
}
