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

        if (request == null) {
            throw new ValidationException("Patient request is required.");
        }

        if (actor == null || actor.isBlank()) {
            throw new ValidationException("Authenticated actor is required.");
        }

        // Validate every field before attempting persistence.
        validator.validateMedicalRecordNumber(request.medicalRecordNumber());
        validator.validateFullName(request.fullName());
        validator.validateDateOfBirth(request.dateOfBirth());

        // Reject duplicate medical record numbers without echoing input values.
        if (patientRepository.existsByMedicalRecordNumber(
                request.medicalRecordNumber())) {
            throw new ValidationException("Medical record number already exists.");
        }

        Patient patient = new Patient(
                request.medicalRecordNumber(),
                request.fullName(),
                request.dateOfBirth()
        );

        Patient savedPatient = patientRepository.save(patient);

        // The audit target is the generated ID, never PHI.
        auditService.record(actor, "PATIENT_CREATE",
                String.valueOf(savedPatient.getId()));

        return PatientResponse.from(savedPatient);
    }

    @Transactional(readOnly = true)
    public PatientResponse get(Long id) {
        // TODO(phase C): load the patient (or 404) and return it as a PatientResponse.
        return PatientResponse.from(requireEntity(id));
    }

    @Transactional(readOnly = true)
    public Patient requireEntity(Long id) {
        // TODO(phase C): return the Patient by id or throw ResourceNotFoundException with a non-enumerating message.

        if (id == null || id <= 0) {
            throw new ResourceNotFoundException("Patient not found.");
        }

        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Patient not found."));
    }
}

