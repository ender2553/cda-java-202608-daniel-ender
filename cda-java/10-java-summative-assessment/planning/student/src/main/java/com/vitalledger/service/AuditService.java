package com.vitalledger.service;

import com.vitalledger.domain.AuditEntry;
import com.vitalledger.dto.AuditResponse;
import com.vitalledger.repository.AuditEntryRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes and reads the tamper-evident audit trail. Audit records never contain PHI.
 */
@Service
public class AuditService {

    private final AuditEntryRepository auditRepository;

    public AuditService(AuditEntryRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Records an audit entry within the caller's transaction (MANDATORY): if there is
     * no active transaction this fails, guaranteeing audit writes are part of the
     * all-or-nothing business transaction rather than silently committing alone.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String actor, String action, String targetRef) {
        // TODO(phase C): persist a new AuditEntry(actor, action, targetRef) via auditRepository, inside the caller's transaction.
        auditRepository.save(new AuditEntry(actor, action, targetRef));
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> recent() {
        // TODO(phase C): return all audit entries newest-first, each mapped via AuditResponse.from.
        return auditRepository.findAllByOrderByOccurredAtDesc()
                .stream()
                .map(AuditResponse::from)
                .toList();
    }
}

