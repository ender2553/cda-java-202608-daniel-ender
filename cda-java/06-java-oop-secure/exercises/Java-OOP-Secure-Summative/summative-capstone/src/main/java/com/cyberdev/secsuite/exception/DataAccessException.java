package com.cyberdev.secsuite.exception;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Repository implementations wrap any lower-level persistence failure in this type -- for a
 * Jdbc*Repository that is Spring's org.springframework.dao.DataAccessException (which
 * JdbcTemplate already translated from the raw java.sql.SQLException), for an
 * InMemory*Repository it is whatever RuntimeException the backing collection threw. Services
 * therefore depend on ONE stable, SecSuite-specific exception type instead of on
 * persistence-layer implementation details, which is exactly what makes swapping
 * InMemory* for Jdbc* in Main a zero-change operation for every service.
 *
 * INSTRUCTOR NOTE: yes, this class deliberately shares its simple name with Spring's
 * org.springframework.dao.DataAccessException. Inside the Jdbc* repositories, Spring's type
 * is therefore always written fully-qualified in the catch clause, and this type is the one
 * imported -- a small but real lesson in reading imports carefully.
 */
public class DataAccessException extends SecSuiteException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
