package com.rti.integration.dto;

/**
 * Service-side report contract. Deliberately does not expose legacy model fields.
 */
public class ServiceReport {

    public String accountId;
    public long balanceCents;
    public int entryCount;

    public ServiceReport() {
    }

    public ServiceReport(String accountId, long balanceCents, int entryCount) {
        this.accountId = accountId;
        this.balanceCents = balanceCents;
        this.entryCount = entryCount;
    }
}
