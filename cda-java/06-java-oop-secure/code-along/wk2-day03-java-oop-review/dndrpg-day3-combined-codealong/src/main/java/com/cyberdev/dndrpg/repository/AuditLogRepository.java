package com.cyberdev.dndrpg.repository;

import com.cyberdev.dndrpg.logging.AuditEntry;

import java.util.List;

public interface AuditLogRepository {
    void save(AuditEntry entry);
    List<AuditEntry> findRecent(int limit);
}
