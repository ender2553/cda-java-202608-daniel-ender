package demo.ledger.domain;

import java.time.OffsetDateTime;

public record AccountHolder(String accountId, String displayName, String taxId, OffsetDateTime createdAt) {
}
