package com.rti.legacy.common;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Append-only in-memory audit trail.
 */
@Component
public class AuditLog {

    private final List<String> entries = new CopyOnWriteArrayList<>();

    public void record(String message) {
        entries.add(message);
    }

    public List<String> entries() {
        return entries;
    }
}
