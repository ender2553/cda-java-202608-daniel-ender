package com.vitalledger.service;

import com.vitalledger.domain.Patient;
import com.vitalledger.dto.CreatePatientRequest;
import com.vitalledger.dto.PatientResponse;
import com.vitalledger.exception.ResourceNotFoundException;
import com.vitalledger.exception.ValidationException;
import com.vitalledger.repository.PatientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final InputValidator validator;
    private final AuditService auditService;

    public PatientService(PatientRepository patientRepository, InputValidator validator,
                          AuditService auditService) {
        this.patientRepository = patientRepository;
        this.validator = validator;
        this.auditService = auditService;
    }

    @Transactional
    public PatientResponse create(CreatePatientRequest request, String actor) {
        // TODO(phase C): fail-closed validate all fields BEFORE persistence, reject duplicate MRN, save the patient, write the PATIENT_CREATE audit entry, and return a PatientResponse.
        throw new UnsupportedOperationException("TODO(phase C)");
    }

    @Transactional(readOnly = true)
    public PatientResponse get(Long id) {
        // TODO(phase C): load the patient (or 404) and return it as a PatientResponse.
        throw new UnsupportedOperationException("TODO(phase C)");
    }

    @Transactional(readOnly = true)
    public Patient requireEntity(Long id) {
        // TODO(phase C): return the Patient by id or throw ResourceNotFoundException with a non-enumerating message.
        throw new UnsupportedOperationException("TODO(phase C)");
    }
}
