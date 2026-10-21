package com.vitalledger.repository;

import com.vitalledger.domain.Patient;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Secure repository for {@link Patient}.
 *
 * <p>All access is <b>parameterized by construction</b>: derived-query methods and
 * the explicit JPQL below both bind values as bound parameters. There is no
 * string-concatenated SQL anywhere in this codebase, so user input can never alter
 * query structure (SQL-injection defense at the data layer).</p>
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Parameterized JPQL — :mrn is a bound parameter, never string-built.
    @Query("select p from Patient p where p.medicalRecordNumber = :mrn")
    Optional<Patient> findByMedicalRecordNumber(@Param("mrn") String medicalRecordNumber);

    boolean existsByMedicalRecordNumber(String medicalRecordNumber);
}
