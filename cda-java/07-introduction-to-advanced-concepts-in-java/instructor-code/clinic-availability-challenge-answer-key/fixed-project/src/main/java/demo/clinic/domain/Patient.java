package demo.clinic.domain;

import java.time.LocalDate;

/** One row of {@code patients}. */
public record Patient(String patientId, String displayName, LocalDate patientSince) {
}
