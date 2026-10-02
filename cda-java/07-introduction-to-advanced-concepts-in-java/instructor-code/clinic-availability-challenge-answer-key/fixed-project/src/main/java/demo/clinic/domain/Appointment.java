package demo.clinic.domain;

import java.time.OffsetDateTime;

/** One row of {@code appointments}. */
public record Appointment(long id, String patientId, String clinicianId, OffsetDateTime startsAt,
                          String reasonCode, String status) {
}
