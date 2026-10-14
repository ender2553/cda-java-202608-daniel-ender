package com.vitalledger.repository;

import com.vitalledger.domain.AuditEntry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Secure repository for {@link AuditEntry}. Parameterized access only.
 */
public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {

    List<AuditEntry> findAllByOrderByOccurredAtDesc();
}
