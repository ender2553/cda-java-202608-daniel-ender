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

    public static final int MAX_SEARCH_RESULTS = 50;

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
        List<Patient> found;
        try {
            found = jdbc.sql("""
                            SELECT patient_id, display_name, patient_since
                            FROM patients
                            WHERE display_name ILIKE :pattern
                            ORDER BY display_name
                            LIMIT :limit""")
                    .param("pattern", "%" + namePart + "%")
                    .param("limit", MAX_SEARCH_RESULTS + 1)
                    .query(PATIENT)
                    .list();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("search patients", "Could not search patients.", ex);
        }
        if (found.size() > MAX_SEARCH_RESULTS) {
            throw new IllegalArgumentException("More than " + MAX_SEARCH_RESULTS + " patients match. Type more of the name.");
        }
        return found;
    }

    public Optional<Patient> findById(String patientId) {
        Validate.patientId(patientId);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT patient_id, display_name, patient_since FROM patients WHERE patient_id = ?")) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Patient(
                        rs.getString("patient_id"),
                        rs.getString("display_name"),
                        rs.getObject("patient_since", LocalDate.class)));
            }
        } catch (SQLException ex) {
            throw DataAccessFailure.logged("load patient", "Could not load that patient.", ex);
        }
    }
}
