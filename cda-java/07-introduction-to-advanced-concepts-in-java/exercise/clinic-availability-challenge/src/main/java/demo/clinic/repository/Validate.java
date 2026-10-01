package demo.clinic.repository;

import java.time.LocalDate;
import java.util.regex.Pattern;

/** Input checks that run before any SQL or file access. Messages are safe to show. */
public final class Validate {

    private static final Pattern PATIENT_ID = Pattern.compile("PAT-\\d{8}");
    private static final Pattern NAME_SEARCH = Pattern.compile("[\\p{L}\\d .'-]{1,64}");

    private Validate() {
    }

    public static String patientId(String value) {
        if (value == null || !PATIENT_ID.matcher(value).matches()) {
            throw new IllegalArgumentException("Patient ID must look like PAT-00000001");
        }
        return value;
    }

    public static String nameSearch(String value) {
        if (value == null || !NAME_SEARCH.matcher(value).matches()) {
            throw new IllegalArgumentException("Search text must be 1-64 letters, digits, spaces, periods, apostrophes or hyphens");
        }
        return value;
    }

    public static int between(int value, int min, int max, String message) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static void dateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("The start date must be on or before the end date");
        }
    }
}
