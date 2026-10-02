package demo.clinic.domain;

import java.time.LocalDateTime;

/**
 * One line of the visit log:
 * {@code visit_id,checked_in_at,patient_id,clinician_id,reason_code,minutes}.
 */
public record Visit(long visitId, LocalDateTime checkedInAt, String patientId, String clinicianId,
                    String reasonCode, int minutes) {

    public static Visit parse(String line) {
        String[] f = line.split(",", -1);
        return new Visit(Long.parseLong(f[0]), LocalDateTime.parse(f[1]), f[2], f[3], f[4], Integer.parseInt(f[5]));
    }

    public String toCsv() {
        return visitId + "," + checkedInAt + "," + patientId + "," + clinicianId + "," + reasonCode + "," + minutes;
    }
}
