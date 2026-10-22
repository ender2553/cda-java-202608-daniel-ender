package com.vitalledger.service;

import com.vitalledger.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Fail-closed, allow-list input validation applied at the service boundary — the
 * authoritative gate, independent of (and in addition to) the controller's bean
 * validation. Everything not explicitly permitted is rejected.
 */
@Component
public class InputValidator {

    /** Allow-list of clinical record categories. */
    private static final Set<String> ALLOWED_RECORD_TYPES = Set.of(
            "PROGRESS_NOTE", "LAB_RESULT", "PRESCRIPTION", "DISCHARGE_SUMMARY", "RADIOLOGY");

    private static final Pattern MRN = Pattern.compile("^MRN-[0-9]{4,10}$");
    private static final Pattern ISO_DATE = Pattern.compile("^[0-9]{4}-[0-9]{2}-[0-9]{2}$");

    private static final int MAX_FULL_NAME_LENGTH = 200;
    private static final int MAX_NOTE_LENGTH = 8000;

    // Defense-in-depth: reject content bearing SQL meta-characters / statement markers.
    // (The repository is already parameterized; this is a belt-and-suspenders signal.)
    private static final Pattern SQL_META = Pattern.compile(
            "(?i)(--|/\\*|\\*/|\\bunion\\b\\s+\\bselect\\b|\\bdrop\\s+table\\b|\\bor\\b\\s+1\\s*=\\s*1)");

    public void validateMedicalRecordNumber(String mrn) {
        // TODO(phase C): fail-closed check that mrn is non-blank and matches the MRN format; throw ValidationException otherwise.
        requireNonBlank(mrn, "Medical record number is required.");

        if (!MRN.matcher(mrn).matches()) {
            throw new ValidationException("Medical record number has an invalid format.");
        }
    }

    public void validateFullName(String fullName) {
        // TODO(phase C): fail-closed check that fullName is non-blank, within max length, and free of SQL meta-markers.
        requireNonBlank(fullName, "Full name is required.");

        if (fullName.length() > MAX_FULL_NAME_LENGTH) {
            throw new ValidationException("Full name exceeds the maximum length.");
        }

        rejectSqlMeta(fullName, "Full name");
    }

    public void validateDateOfBirth(String dob) {
        // TODO(phase C): fail-closed check that dob is non-blank and an ISO date (YYYY-MM-DD).
        requireNonBlank(dob, "Date of birth is required.");

        if (!ISO_DATE.matcher(dob).matches()) {
            throw new ValidationException("Date of birth must use YYYY-MM-DD format.");
        }

        try {
            LocalDate.parse(dob);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Date of birth must be a valid calendar date.");
        }
    }

    public void validateRecordType(String recordType) {
        // TODO(phase C): fail-closed allow-list check against ALLOWED_RECORD_TYPES; message must not echo the rejected value.
        requireNonBlank(recordType, "Record type is required.");

        if (!ALLOWED_RECORD_TYPES.contains(recordType)) {
            throw new ValidationException("Record type is not permitted.");
        }
    }

    public void validateNote(String note) {
        // TODO(phase C): fail-closed check that note is non-blank, within max length, and free of SQL meta-markers.
        requireNonBlank(note, "Clinical note is required.");

        if (note.length() > MAX_NOTE_LENGTH) {
            throw new ValidationException("Clinical note exceeds the maximum length.");
        }

        rejectSqlMeta(note, "Clinical note");
    }

    private void rejectSqlMeta(String value, String field) {
        // TODO(phase C): throw ValidationException if value contains SQL meta-markers (defense in depth over parameterized queries).
        if (SQL_META.matcher(value).find()) {
            throw new ValidationException(field + " contains disallowed content.");
        }
    }

    private void requireNonBlank(String value, String message) {
        // TODO(phase C): throw ValidationException(message) when value is null or blank.
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
}

