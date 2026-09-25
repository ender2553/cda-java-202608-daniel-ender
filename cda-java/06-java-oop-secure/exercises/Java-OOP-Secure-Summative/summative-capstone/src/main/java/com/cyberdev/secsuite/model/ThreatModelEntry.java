package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One identified threat inside a threat model, classified by STRIDE category (maps to
 * "threat_model_entry").
 *
 * ENTITY: equal by id. Two entries with identical text in the same category are still two
 * separately-tracked threats (e.g. the same spoofing risk on two different endpoints).
 */
public final class ThreatModelEntry {

    private final Long id;
    private final Long threatModelId;
    private final StrideCategory strideCategory;
    private final String description;
    private final String mitigation;
    private final ThreatModelEntryStatus status;

    public ThreatModelEntry(Long id, Long threatModelId, StrideCategory strideCategory, String description,
                            String mitigation, ThreatModelEntryStatus status) {
        if (id == null) {
            throw new ValidationException("threat model entry id must not be null");
        }
        if (threatModelId == null) {
            throw new ValidationException("threatModelId must not be null");
        }
        if (strideCategory == null) {
            throw new ValidationException("strideCategory must not be null");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("description must not be blank");
        }
        if (status == null) {
            throw new ValidationException("status must not be null");
        }
        this.id = id;
        this.threatModelId = threatModelId;
        this.strideCategory = strideCategory;
        this.description = description.trim();
        this.mitigation = (mitigation == null || mitigation.isBlank()) ? null : mitigation.trim();
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public ThreatModelEntry withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new ThreatModelEntry(id, threatModelId, strideCategory, description, mitigation, status);
    }

    public Long getThreatModelId() {
        return threatModelId;
    }

    public StrideCategory getStrideCategory() {
        return strideCategory;
    }

    public String getDescription() {
        return description;
    }

    /** May be null (no mitigation decided yet). */
    public String getMitigation() {
        return mitigation;
    }

    public ThreatModelEntryStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreatModelEntry other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ThreatModelEntry{" + strideCategory + ": " + description + " " + status + "}";
    }
}
