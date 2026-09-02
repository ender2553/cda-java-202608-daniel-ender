package org.example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A single row in the security risk register. Unlike ThreatIndicator and
 * Dependency, a risk register entry is INTENTIONALLY mutable over its
 * lifetime -- analysts update its status and append notes as they work
 * the risk. But "intentionally mutable" is not the same as "leaky."
 *
 * USER STORY: As a security analyst, I need to update a risk's status and
 * append investigation notes over time, WITHOUT any external code being
 * able to reach in and rewrite the entire notes history directly.
 *
 * ACCEPTANCE CRITERIA:
 *  1. relatedCveIds is set once at construction and defensively copied
 *     (it never changes after that -- the finding it's based on is fixed).
 *  2. notes grows only through addNote(String); there is no setNotes().
 *  3. getNotes() never returns the live internal list.
 *  4. status can change only through updateStatus(String), which validates
 *     the new value against the allow-listed set of statuses.
 */
public final class RiskRegisterEntry {

    public static final List<String> ALLOWED_STATUSES =
            Collections.unmodifiableList(java.util.Arrays.asList("OPEN", "MITIGATED", "ACCEPTED", "CLOSED"));

    private final String riskId;
    private final String assetId;
    private final String description;
    private final String severity; // CRITICAL, HIGH, MEDIUM, LOW
    private final double cvssScore;
    private final double epssProbability;
    private final List<String> relatedCveIds; // fixed at construction, defensively copied

    private String status; // mutable, but only via updateStatus()
    private final List<String> notes;         // mutable, but only via addNote()

    /**
     * TODO (ACCEPTANCE CRITERION 1): implement this constructor.
     *   - Assign riskId, assetId, description, and severity directly,
     *     each guarded with Objects.requireNonNull(...).
     *   - Assign cvssScore and epssProbability directly (primitives,
     *     nothing to defend).
     *   - For relatedCveIds: defensively copy into an unmodifiable list,
     *     same pattern as ThreatIndicator.tags and Dependency.knownCves
     *     (treat a null argument as an empty list).
     *   - Initialize status to "OPEN" (the only legal starting status).
     *   - Initialize notes to a new, empty, MUTABLE ArrayList -- unlike
     *     relatedCveIds, notes is allowed to grow over the object's
     *     lifetime, just only through addNote(String) below.
     */
    public RiskRegisterEntry(String riskId, String assetId, String description, String severity,
                             double cvssScore, double epssProbability, List<String> relatedCveIds) {

        this.riskId = Objects.requireNonNull(riskId);
        this.assetId = Objects.requireNonNull(assetId);
        this.description = Objects.requireNonNull(description);
        this.severity = Objects.requireNonNull(severity);

        this.cvssScore = cvssScore;
        this.epssProbability = epssProbability;

        List<String> copy = new ArrayList<>(
                relatedCveIds == null ? Collections.emptyList() : relatedCveIds
        );

        this.relatedCveIds = Collections.unmodifiableList(copy);

        this.status = "OPEN";
        this.notes = new ArrayList<>();
    }

    public String getRiskId() { return riskId; }
    public String getAssetId() { return assetId; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public double getCvssScore() { return cvssScore; }
    public double getEpssProbability() { return epssProbability; }
    public List<String> getRelatedCveIds() { return relatedCveIds; }
    public String getStatus() { return status; }

    /**
     * TODO (ACCEPTANCE CRITERION 3): return a defensive COPY of notes
     * (e.g. `new ArrayList<>(notes)`), never the live internal list --
     * unlike relatedCveIds, notes is not wrapped as unmodifiable at
     * construction because it needs to keep growing internally via
     * addNote(...), so the defense has to happen here, at the getter,
     * on every call.
     */
    public List<String> getNotes() {
        return new ArrayList<>(notes);
    }

    /**
     * TODO (ACCEPTANCE CRITERION 2): validate that note is non-null and
     * non-blank (throw IllegalArgumentException if it is), then append it
     * to the internal notes list. This is the ONLY way notes should ever
     * grow -- there must be no setNotes(...) method anywhere in this class.
     */
    public void addNote(String note) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("Note cannot be null or blank");
        }

        notes.add(note);
    }

    /**
     * TODO (ACCEPTANCE CRITERION 4): validate that newStatus is non-null
     * and is a member of ALLOWED_STATUSES (throw IllegalArgumentException
     * if not -- this is an allow-list check, the same pattern used
     * throughout InputValidator), then assign it to this.status.
     */
    public void updateStatus(String newStatus) {
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        this.status = newStatus;
    }

    @Override
    public String toString() {
        return "RiskRegisterEntry{" + riskId + ", asset=" + assetId + ", severity=" + severity +
                ", cvss=" + cvssScore + ", epss=" + epssProbability + ", status=" + status + "}";
    }
}
