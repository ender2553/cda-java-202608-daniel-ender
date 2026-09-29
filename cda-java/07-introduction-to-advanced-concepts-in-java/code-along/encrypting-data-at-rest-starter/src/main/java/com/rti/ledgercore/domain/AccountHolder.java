package com.rti.ledgercore.domain;

import java.time.Instant;

/**
 * A LedgerCore account holder. {@code taxId} is the sensitive field the lab
 * asks you to protect at rest — see {@code crypto.FieldCipher}.
 */
public record AccountHolder(String accountId, String displayName, String taxId, Instant createdAt) {
}
