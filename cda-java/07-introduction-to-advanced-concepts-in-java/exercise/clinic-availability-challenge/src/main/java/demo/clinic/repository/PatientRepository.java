package demo.clinic.repository;

import demo.clinic.domain.Patient;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** The only path to {@code patients}. */
@Repository
public class PatientRepository {

    private static final RowMapper<Patient> PATIENT = (rs, rowNum) -> new Patient(
            rs.getString("patient_id"),
            rs.getString("display_name"),
            rs.getObject("patient_since", LocalDate.class));

    private final JdbcClient jdbc;
    private final DataSource dataSource;

    public PatientRepository(JdbcClient jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    public List<Patient> searchByName(String namePart) {
        Validate.nameSearch(namePart);
        try {
            return jdbc.sql("""
                            SELECT patient_id, display_name, patient_since
                            FROM patients
                            WHERE display_name ILIKE :pattern
                            ORDER BY display_name""")
                    .param("pattern", "%" + namePart + "%")
                    .query(PATIENT)
                    .list();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("search patients", "Could not search patients.", ex);
        }
    }

    public Optional<Patient> findById(String patientId) {
        Validate.patientId(patientId);
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT patient_id, display_name, patient_since FROM patients WHERE patient_id = ?");
            ps.setString(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }
            Patient patient = new Patient(
                    rs.getString("patient_id"),
                    rs.getString("display_name"),
                    rs.getObject("patient_since", LocalDate.class));
            rs.close();
            ps.close();
            conn.close();
            return Optional.of(patient);
        } catch (SQLException ex) {
            throw DataAccessFailure.logged("load patient", "Could not load that patient.", ex);
        }
    }
}
