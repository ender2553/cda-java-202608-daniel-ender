package com.cyberdev.secsuite.repository.inmemory;

import java.util.Map;

final class InMemoryIds {

    private InMemoryIds() {
    }

    static long nextId(Map<Long, ?> rows) {
        return rows.keySet().stream().mapToLong(Long::longValue).max().orElse(0L) + 1L;
    }
}
