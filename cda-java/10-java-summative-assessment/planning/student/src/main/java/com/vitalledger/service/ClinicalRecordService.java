package com.vitalledger.service;

import com.vitalledger.domain.ClinicalRecord;
import com.vitalledger.dto.CreateRecordRequest;
import com.vitalledger.dto.RecordResponse;
import com.vitalledger.exception.ResourceNotFoundException;
import com.vitalledger.repository.ClinicalRecordRepository;
import com.vitalledger.repository.PatientRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicalRecordService {

    private final ClinicalRecordRepository recordRepository;
    private final PatientRepository patientRepository;
    private final InputValidator validator;
    private final AuditService auditService;

    public ClinicalRecordService(ClinicalRecordRepository recordRepository,
                                 PatientRepository patientRepository,
                                 InputValidator validator,
                                 AuditService auditService) {
        this.recordRepository = recordRepository;
        this.patientRepository = patientRepository;
        this.validator = validator;
        this.auditService = auditService;
    }

    /**
     * Secure multi-step transaction: validate → persist record → write audit entry,
     * all-or-nothing. Because both the record save and the audit write share one
     * {@code @Transactional} boundary, a failure in either rolls back the whole unit
     * (no orphan record without its audit trail, and vice versa).
     */
    @Transactional
    public RecordResponse create(Long patientId, CreateRecordRequest request, String actor) {
        // TODO(phase C): validate recordType + note (fail-closed), verify the patient exists, save the record, and write the RECORD_CREATE audit entry in the SAME transaction.
        throw new UnsupportedOperationException("TODO(phase C)");
    }

    @Transactional(readOnly = true)
    public List<RecordResponse> listForPatient(Long patientId) {
        // TODO(phase C): verify the patient exists, then return that patient's records mapped to RecordResponse.
        throw new UnsupportedOperationException("TODO(phase C)");
    }

    @Transactional(readOnly = true)
    public RecordResponse get(Long id) {
        // TODO(phase C): load the record (or 404) and return it as a RecordResponse.
        throw new UnsupportedOperationException("TODO(phase C)");
    }

    @Transactional(readOnly = true)
    public ClinicalRecord requireEntity(Long id) {
        // TODO(phase C): return the ClinicalRecord by id or throw ResourceNotFoundException with a non-enumerating message.
        throw new UnsupportedOperationException("TODO(phase C)");
    }

    @Transactional
    public void delete(Long id, String actor) {
        // TODO(phase C): load the record (or 404), delete it, and write the RECORD_DELETE audit entry in the same transaction.
    }
}
