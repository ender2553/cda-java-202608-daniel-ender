package demo.ledger.domain;

import java.time.OffsetDateTime;

/** What a list or search is allowed to show: no tax ID, encrypted or otherwise. */
public record HolderSummary(String accountId, String displayName, OffsetDateTime createdAt) {
}
