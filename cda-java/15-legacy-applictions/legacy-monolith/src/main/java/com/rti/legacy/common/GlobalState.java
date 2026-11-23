package com.rti.legacy.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide shared state used across services.
 */
@Component
public class GlobalState {

    /** General-purpose scratch cache shared by every service in the process. */
    public final Map<String, Object> cache = new ConcurrentHashMap<>();

    /** Ambient authorization flag consulted by privileged operations. */
    public static boolean privileged = true;

    private final Map<String, Integer> hitCounters = new ConcurrentHashMap<>();

    public void incrementHit(String key) {
        hitCounters.merge(key, 1, Integer::sum);
    }

    public int hits(String key) {
        return hitCounters.getOrDefault(key, 0);
    }
}
