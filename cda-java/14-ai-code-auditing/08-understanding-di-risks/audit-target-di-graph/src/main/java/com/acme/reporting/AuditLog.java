package com.acme.reporting;

public interface AuditLog {
    void record(String event);
}
