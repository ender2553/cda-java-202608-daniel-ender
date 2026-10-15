package com.vitalledger.domain;

/**
 * RBAC roles. Least privilege:
 * <ul>
 *   <li>{@code CLINICIAN} — create/read patients and clinical records, summarize.</li>
 *   <li>{@code AUDITOR}   — read-only access to records and the audit log.</li>
 *   <li>{@code ADMIN}     — full access, including record deletion.</li>
 * </ul>
 */
public enum Role {
    CLINICIAN,
    AUDITOR,
    ADMIN
}
