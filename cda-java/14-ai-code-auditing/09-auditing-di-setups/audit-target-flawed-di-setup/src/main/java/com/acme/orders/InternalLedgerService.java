package com.acme.orders;

import org.springframework.stereotype.Service;

@Service
public class InternalLedgerService {

    public void postEntry(String account, long amountCents) {
        // Posts directly to the financial ledger.
    }

    public void adjustBalance(String account, long deltaCents) {
        // Privileged manual balance adjustment.
    }

    public long readBalance(String account) {
        return 0L;
    }
}
