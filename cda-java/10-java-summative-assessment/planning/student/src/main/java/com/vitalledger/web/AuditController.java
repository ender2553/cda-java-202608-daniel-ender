package com.vitalledger.web;

import com.vitalledger.dto.AuditResponse;
import com.vitalledger.service.AuditService;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Audit-log surface. Read-only, and restricted to AUDITOR and ADMIN — a CLINICIAN
 * cannot read the audit trail. Audit rows carry no PHI.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    public List<AuditResponse> recent() {
        // TODO(phase D): return auditService.recent() — endpoint restricted to AUDITOR/ADMIN by @PreAuthorize.
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
