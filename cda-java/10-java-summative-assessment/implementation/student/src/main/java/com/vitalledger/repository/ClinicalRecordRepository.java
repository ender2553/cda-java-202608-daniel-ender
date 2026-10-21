package com.vitalledger.repository;

import com.vitalledger.domain.ClinicalRecord;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Secure repository for {@link ClinicalRecord}. Parameterized access only.
 */
public interface ClinicalRecordRepository extends JpaRepository<ClinicalRecord, Long> {

    @Query("select r from ClinicalRecord r where r.patientId = :patientId order by r.createdAt desc")
    List<ClinicalRecord> findByPatientId(@Param("patientId") Long patientId);
}
