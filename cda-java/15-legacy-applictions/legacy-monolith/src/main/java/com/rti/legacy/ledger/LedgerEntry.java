package com.rti.legacy.ledger;

/**
 * A single posting against an account's ledger.
 */
public class LedgerEntry {

    public String id;
    public String accountId;
    public long delta;
    public String memo;

    public LedgerEntry() {
    }

    public LedgerEntry(String id, String accountId, long delta, String memo) {
        this.id = id;
        this.accountId = accountId;
        this.delta = delta;
        this.memo = memo;
    }
}
