package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.time.Instant;
import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * A STRIDE threat model for one asset (maps to "threat_model"). The individual threats live in
 * threat_model_entry rows (one-to-many), not in a list field here -- the same "no repeating
 * groups" discipline as Component.
 *
 * ENTITY: equal by id.
 */
public final class ThreatModel {

    private final Long id;
    private final Long assetId;
    private final String title;
    private final String description;
    private final Instant createdAt;

    public ThreatModel(Long id, Long assetId, String title, String description, Instant createdAt) {
        if (id == null) {
            throw new ValidationException("threat model id must not be null");
        }
        if (assetId == null) {
            throw new ValidationException("assetId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("title must not be blank");
        }
        if (createdAt == null) {
            throw new ValidationException("createdAt must not be null");
        }
        this.id = id;
        this.assetId = assetId;
        this.title = title.trim();
        this.description = (description == null || description.isBlank()) ? null : description.trim();
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public ThreatModel withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new ThreatModel(id, assetId, title, description, createdAt);
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getTitle() {
        return title;
    }

    /** May be null. */
    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThreatModel other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ThreatModel{" + title + "}";
    }
}
