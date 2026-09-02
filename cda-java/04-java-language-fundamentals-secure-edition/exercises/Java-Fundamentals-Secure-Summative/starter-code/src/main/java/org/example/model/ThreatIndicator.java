package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A single indicator of compromise (IOC) ingested from an external threat
 * intelligence feed (untrusted input -- see ThreatIntelFeedParser).
 *
 * USER STORY: As a security analyst, I need ThreatIndicator objects to be
 * tamper-proof once created, so that no other part of the application
 * (accidentally or maliciously) can rewrite an indicator's tag list after
 * it has been recorded, which would corrupt the risk register.
 *
 * ACCEPTANCE CRITERIA:
 *  1. GIVEN a caller passes a mutable List<String> into the constructor
 *     WHEN the caller mutates that original list afterward
 *     THEN the ThreatIndicator's internal state must NOT change.
 *  2. GIVEN code outside this class calls getTags()
 *     WHEN that code attempts to add/remove/clear the returned list
 *     THEN either the internal state is unaffected (defensive copy) OR an
 *          UnsupportedOperationException is thrown (unmodifiable view) --
 *          either approach is acceptable, but returning the live internal
 *          list is NOT.
 *  3. The class has no setters; every field is final.
 */
public final class ThreatIndicator {

    private final String indicatorValue; // e.g. "203.0.113.5", "evil.example.com", a SHA-256 hash
    private final String indicatorType;  // allow-listed: IP, DOMAIN, FILE_HASH
    private final int confidence;        // 0-100
    private final String source;
    private final List<String> tags;     // MUTABLE COLLECTION -- must be defended

    /**
     * TODO (ACCEPTANCE CRITERION 1): implement this constructor.
     *   - Assign indicatorValue, indicatorType, confidence, and source
     *     directly (use Objects.requireNonNull(...) on the three String
     *     fields, matching the style used elsewhere in this project).
     *   - For tags: do NOT store the caller's list reference directly.
     *     Copy its contents into a new, private list first (treat a null
     *     tags argument as an empty list), then make that private copy
     *     unmodifiable before assigning it to this.tags. This is the
     *     "defensive copy" pattern -- see PortScanResult's Javadoc for
     *     why it matters, and Dependency in this same package for a
     *     second worked example of the identical pattern.
     */
    public ThreatIndicator(String indicatorValue, String indicatorType, int confidence,
                            String source, List<String> tags) {
        throw new UnsupportedOperationException(
                "TODO: implement ThreatIndicator constructor with defensive copy of tags");
    }

    public String getIndicatorValue() { return indicatorValue; }
    public String getIndicatorType() { return indicatorType; }
    public int getConfidence() { return confidence; }
    public String getSource() { return source; }

    /**
     * TODO (ACCEPTANCE CRITERION 2): implement this getter so that a
     * caller can never mutate this object's internal tags list through
     * the reference it returns. If the constructor above already stores
     * tags as an unmodifiable wrapper, the simplest correct
     * implementation here is a single-line direct return of the field.
     */
    public List<String> getTags() {
        throw new UnsupportedOperationException("TODO: implement getTags() defensive return");
    }

    @Override
    public String toString() {
        return "ThreatIndicator{" + indicatorType + ":" + indicatorValue +
                ", confidence=" + confidence + ", source='" + source + "', tags=" + tags + "}";
    }
}
