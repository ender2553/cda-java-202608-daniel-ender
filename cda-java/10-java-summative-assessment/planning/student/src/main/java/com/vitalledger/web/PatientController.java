package com.vitalledger.web;

import com.vitalledger.dto.CreatePatientRequest;
import com.vitalledger.dto.PatientResponse;
import com.vitalledger.service.PatientService;

import jakarta.validation.Valid;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** Create a patient — CLINICIAN or ADMIN only. */
    @PostMapping
    @PreAuthorize("hasAnyRole('CLINICIAN','ADMIN')")
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request,
                                                  Authentication authentication) {
        // TODO(phase D): delegate to patientService.create(...) and return 201 Created with a Location header; RBAC via @PreAuthorize.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    /** Read a patient — any authenticated clinical role. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLINICIAN','AUDITOR','ADMIN')")
    public PatientResponse get(@PathVariable Long id) {
        // TODO(phase D): return patientService.get(id); readable by CLINICIAN/AUDITOR/ADMIN per @PreAuthorize.
        throw new UnsupportedOperationException("TODO(phase D)");
    }
}
