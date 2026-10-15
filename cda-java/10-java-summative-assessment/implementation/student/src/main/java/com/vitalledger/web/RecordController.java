package com.vitalledger.web;

import com.vitalledger.dto.CreateRecordRequest;
import com.vitalledger.dto.RecordResponse;
import com.vitalledger.dto.SummaryResponse;
import com.vitalledger.mcp.SummarizerTool;
import com.vitalledger.service.ClinicalRecordService;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clinical-record endpoints. RBAC is enforced per method with {@code @PreAuthorize}:
 * clinicians author and summarize, auditors read, admins delete.
 */
@RestController
public class RecordController {

    private final ClinicalRecordService recordService;
    private final SummarizerTool summarizerTool;

    public RecordController(ClinicalRecordService recordService, SummarizerTool summarizerTool) {
        this.recordService = recordService;
        this.summarizerTool = summarizerTool;
    }

    /** Create a record for a patient (transactional create-with-audit) — CLINICIAN only. */
    @PostMapping("/api/patients/{patientId}/records")
    @PreAuthorize("hasRole('CLINICIAN')")
    public ResponseEntity<RecordResponse> create(@PathVariable Long patientId,
                                                 @Valid @RequestBody CreateRecordRequest request,
                                                 Authentication authentication) {
        // TODO(phase D): delegate to recordService.create(...) and return 201 Created with a Location header; CLINICIAN only.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /** List a patient's records — CLINICIAN, AUDITOR, ADMIN. */
    @GetMapping("/api/patients/{patientId}/records")
    @PreAuthorize("hasAnyRole('CLINICIAN','AUDITOR','ADMIN')")
    public List<RecordResponse> list(@PathVariable Long patientId) {
        // TODO(phase D): return recordService.listForPatient(patientId); CLINICIAN/AUDITOR/ADMIN per @PreAuthorize.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /** Read a single record — CLINICIAN, AUDITOR, ADMIN. */
    @GetMapping("/api/records/{id}")
    @PreAuthorize("hasAnyRole('CLINICIAN','AUDITOR','ADMIN')")
    public RecordResponse get(@PathVariable Long id) {
        // TODO(phase D): return recordService.get(id); CLINICIAN/AUDITOR/ADMIN per @PreAuthorize.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /** Delete a record — ADMIN only. */
    @DeleteMapping("/api/records/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        // TODO(phase D): delegate to recordService.delete(...) and return 204 No Content; ADMIN only.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /** Summarize a record via the secure MCP-style tool — CLINICIAN or ADMIN. */
    @PostMapping("/api/records/{id}/summarize")
    @PreAuthorize("hasAnyRole('CLINICIAN','ADMIN')")
    public SummaryResponse summarize(@PathVariable Long id) {
        // TODO(phase D): invoke the secure MCP-style summarizerTool.summarize(id); CLINICIAN/ADMIN per @PreAuthorize.
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
