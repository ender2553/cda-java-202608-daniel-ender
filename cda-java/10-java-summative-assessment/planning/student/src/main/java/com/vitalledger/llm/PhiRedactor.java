package com.vitalledger.llm;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Minimizes PHI before any model call: strips direct identifiers so the text that
 * leaves the trust boundary carries the minimum necessary clinical signal.
 *
 * <p>Removes the patient's name and date of birth (passed in explicitly), then masks
 * common identifier patterns (SSN, phone, email, MRN) that might appear free-text
 * inside a note. This is defense in depth on top of "audit rows hold no PHI".</p>
 */
@Component
public class PhiRedactor {

    private static final Pattern SSN   = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern PHONE = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
    private static final Pattern EMAIL = Pattern.compile("\\b[\\w.+-]+@[\\w-]+\\.[\\w.-]+\\b");
    private static final Pattern MRN   = Pattern.compile("\\bMRN-?\\d{4,10}\\b", Pattern.CASE_INSENSITIVE);

    /**
     * @param note        the clinical note (PHI)
     * @param patientName the patient's full name to scrub from the note
     * @param dateOfBirth the patient's DOB to scrub from the note
     * @return redacted text safe(r) to send to a model
     */
    public String redact(String note, String patientName, String dateOfBirth) {
        // TODO(phase E): scrub the patient's name (and each token) and DOB, then mask SSN/phone/email/MRN patterns; return "" for null input.
        throw new UnsupportedOperationException("TODO(phase E)");
    }

    private static String maskLiteral(String haystack, String literal) {
        // TODO(phase E): case-insensitively replace a literal occurrence in haystack with a [REDACTED-*] marker.
        throw new UnsupportedOperationException("TODO(phase E)");
    }
}
