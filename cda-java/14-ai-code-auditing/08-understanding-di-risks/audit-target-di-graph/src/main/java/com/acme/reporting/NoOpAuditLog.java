package com.acme.reporting;

public class NoOpAuditLog implements AuditLog {

    @Override
    public void record(String event) {
        // Nothing is recorded.
    }
}
