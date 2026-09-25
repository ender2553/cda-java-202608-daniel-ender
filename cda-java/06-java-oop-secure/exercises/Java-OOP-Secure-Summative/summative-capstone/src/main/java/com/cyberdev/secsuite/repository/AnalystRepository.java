package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.Analyst;

import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Persistence for analyst accounts (table "analyst"). Used by AuthService (SEC-13/SEC-14) and
 * by RiskRegisterService to confirm a risk owner exists.
 *
 * Contract shared by generated-id repositories: save() is an INSERT and returns an immutable
 * persisted copy containing the generated positive id. The input uses id 0 and is not mutated.
 * A key collision surfaces as com.cyberdev.secsuite.exception.DataAccessException; find*
 * methods return Optional/empty lists, never null.
 */
public interface AnalystRepository {

    Analyst save(Analyst analyst);

    Optional<Analyst> findByUsername(String username);

    Optional<Analyst> findById(Long id);
}
