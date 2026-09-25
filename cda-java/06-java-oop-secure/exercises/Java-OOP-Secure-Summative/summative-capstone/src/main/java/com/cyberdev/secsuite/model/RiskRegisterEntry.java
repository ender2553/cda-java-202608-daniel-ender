package com.cyberdev.secsuite.model;

import com.cyberdev.secsuite.exception.ValidationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * One tracked risk (maps to "risk_register_entry"). A risk is always about an asset; it MAY
 * originate from a specific scan finding (scanFindingId non-null) or be a manually-raised risk
 * with no finding behind it (e.g. "no MFA on the VPN portal"), which is why that FK is
 * nullable.
 *
 * riskScore is stored even though it equals likelihood * impact. That is a deliberate,
 * CONSTRAINED derived column: the schema has CHECK (risk_score = likelihood * impact) so the
 * database itself makes the redundancy impossible to get out of sync, and this constructor
 * enforces the same invariant in Java. It exists so the register can be sorted/indexed by
 * score directly. See the comment on the table in schema/schema.sql.
 *
 * ENTITY: equal by id.
 */
public final class RiskRegisterEntry {

    private final Long id;
    private final Long assetId;
    private final Long scanFindingId;
    private final String title;
    private final String description;
    private final int likelihood;
    private final int impact;
    private final int riskScore;
    private final RiskStatus status;
    private final Long ownerAnalystId;
    private final LocalDate dueDate;
    private final Instant createdAt;

    public RiskRegisterEntry(Long id, Long assetId, Long scanFindingId, String title, String description,
                             int likelihood, int impact, int riskScore, RiskStatus status,
                             Long ownerAnalystId, LocalDate dueDate, Instant createdAt) {
        if (id == null) {
            throw new ValidationException("risk id must not be null");
        }
        if (assetId == null) {
            throw new ValidationException("assetId must not be null");
        }
        if (title == null || title.isBlank()) {
            throw new ValidationException("title must not be blank");
        }
        if (likelihood < 1 || likelihood > 5) {
            throw new ValidationException("likelihood must be between 1 and 5, was " + likelihood);
        }
        if (impact < 1 || impact > 5) {
            throw new ValidationException("impact must be between 1 and 5, was " + impact);
        }
        if (riskScore != likelihood * impact) {
            throw new ValidationException("riskScore must equal likelihood * impact ("
                    + (likelihood * impact) + "), was " + riskScore);
        }
        if (status == null) {
            throw new ValidationException("status must not be null");
        }
        if (createdAt == null) {
            throw new ValidationException("createdAt must not be null");
        }
        this.id = id;
        this.assetId = assetId;
        this.scanFindingId = scanFindingId;
        this.title = title.trim();
        this.description = (description == null || description.isBlank()) ? null : description.trim();
        this.likelihood = likelihood;
        this.impact = impact;
        this.riskScore = riskScore;
        this.status = status;
        this.ownerAnalystId = ownerAnalystId;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public RiskRegisterEntry withId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("id must be positive");
        }
        return new RiskRegisterEntry(id, assetId, scanFindingId, title, description, likelihood, impact,
                riskScore, status, ownerAnalystId, dueDate, createdAt);
    }

    public Long getAssetId() {
        return assetId;
    }

    /** Null for a manually-raised risk that did not come from a scan finding. */
    public Long getScanFindingId() {
        return scanFindingId;
    }

    public String getTitle() {
        return title;
    }

    /** May be null. */
    public String getDescription() {
        return description;
    }

    public int getLikelihood() {
        return likelihood;
    }

    public int getImpact() {
        return impact;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskStatus getStatus() {
        return status;
    }

    /** May be null (unassigned). */
    public Long getOwnerAnalystId() {
        return ownerAnalystId;
    }

    /** May be null. */
    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RiskRegisterEntry other)) return false;
        return id != null && id > 0 && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RiskRegisterEntry{" + title + " score=" + riskScore + " " + status + "}";
    }
}
