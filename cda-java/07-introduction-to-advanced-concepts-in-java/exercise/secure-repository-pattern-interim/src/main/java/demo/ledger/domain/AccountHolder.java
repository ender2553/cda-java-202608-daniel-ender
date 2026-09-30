package demo.ledger.domain;

import java.time.OffsetDateTime;

/**
 * The full account holder, including the decrypted tax ID. Returned only by
 * the single-holder lookup; lists and searches return {@link HolderSummary}.
 */
public record AccountHolder(String accountId, String displayName, String taxId, OffsetDateTime createdAt) {

    /** "***-**-6789": the only form of the tax ID that should ever reach a screen. */
    public String maskedTaxId() {
        return "***-**-" + taxId.substring(taxId.length() - 4);
    }

    /** A record's default toString() prints every field, so one stray log line would leak the tax ID. */
    @Override
    public String toString() {
        return "AccountHolder[accountId=" + accountId + ", displayName=" + displayName
                + ", taxId=" + maskedTaxId() + ", createdAt=" + createdAt + "]";
    }
}
