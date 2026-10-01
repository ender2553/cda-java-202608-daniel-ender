package demo.clinic.repository;

import demo.clinic.domain.Appointment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** The only path to {@code appointments}. */
@Repository
public class AppointmentRepository {

    private static final RowMapper<Appointment> APPOINTMENT = (rs, rowNum) -> new Appointment(
            rs.getLong("id"),
            rs.getString("patient_id"),
            rs.getString("clinician_id"),
            rs.getObject("starts_at", OffsetDateTime.class),
            rs.getString("reason_code"),
            rs.getString("status"));

    private final JdbcClient jdbc;

    public AppointmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Appointment> findUpcoming(int limit) {
        try {
            return jdbc.sql("""
                            SELECT id, patient_id, clinician_id, starts_at, reason_code, status
                            FROM appointments
                            WHERE status = 'BOOKED'
                            ORDER BY starts_at
                            LIMIT :limit""")
                    .param("limit", limit)
                    .query(APPOINTMENT)
                    .list();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("list upcoming appointments", "Could not load appointments.", ex);
        }
    }

    public Optional<Appointment> nextBookedFor(String patientId) {
        Validate.patientId(patientId);
        try {
            return jdbc.sql("""
                            SELECT id, patient_id, clinician_id, starts_at, reason_code, status
                            FROM appointments
                            WHERE patient_id = :patientId AND status = 'BOOKED'
                            ORDER BY starts_at
                            LIMIT 1""")
                    .param("patientId", patientId)
                    .query(APPOINTMENT)
                    .optional();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("find next appointment", "Could not load appointments.", ex);
        }
    }

    public void markCheckedIn(long appointmentId) {
        int rows;
        try {
            rows = jdbc.sql("UPDATE appointments SET status = 'CHECKED_IN' WHERE id = :id AND status = 'BOOKED'")
                    .param("id", appointmentId)
                    .update();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("check in", "Could not check the patient in.", ex);
        }
        if (rows == 0) {
            throw new IllegalArgumentException("That appointment is already checked in");
        }
    }
}
