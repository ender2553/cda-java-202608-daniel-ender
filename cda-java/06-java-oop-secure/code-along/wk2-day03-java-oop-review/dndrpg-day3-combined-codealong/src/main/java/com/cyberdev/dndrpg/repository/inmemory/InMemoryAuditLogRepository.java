package com.cyberdev.dndrpg.repository.inmemory;

import com.cyberdev.dndrpg.logging.AuditEntry;
import com.cyberdev.dndrpg.repository.AuditLogRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Profile("!jdbc")
public final class InMemoryAuditLogRepository implements AuditLogRepository {
    private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();

    @Override
    public void save(AuditEntry entry) {
        entries.add(entry);
    }

    @Override
    public List<AuditEntry> findRecent(int limit) {
        List<AuditEntry> copy = new ArrayList<>(entries);
        Collections.reverse(copy);
        return copy.subList(0, Math.min(limit, copy.size()));
    }
}
