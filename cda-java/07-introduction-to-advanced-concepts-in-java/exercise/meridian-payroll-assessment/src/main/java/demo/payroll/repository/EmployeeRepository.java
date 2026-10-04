package demo.payroll.repository;

import demo.payroll.crypto.BankCrypto;
import demo.payroll.domain.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import demo.payroll.domain.employeeSummary;
import demo.payroll.crypto.PinHasher;

import java.util.List;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbc;
    private final PinHasher pinHasher;
    private final BankCrypto crypto;
    private final RowMapper<Employee> mapper;

    public EmployeeRepository(
            JdbcTemplate jdbc, BankCrypto crypto, PinHasher
                    pinHasher) {
        this.jdbc = jdbc;
        this.pinHasher = pinHasher;
        this.crypto = crypto;
        this.mapper = (rs, rowNum) -> new Employee(
                rs.getString("employee_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("dept_code"),
                rs.getString("title"),
                rs.getBigDecimal("annual_salary"),
                rs.getBoolean("active"),
                rs.getString("ssn"),
                crypto.decrypt(rs.getString("bank_account")),
                null, // PIN is not needed in the employee profile
                rs.getString("hr_notes"));
    }

    public List<employeeSummary> search(String prefix, String sortColumn) {
        String safeSort = switch (sortColumn) {
            case "employee_id" -> "employee_id";
            case "first_name" -> "first_name";
            case "last_name" -> "last_name";
            case "dept_code" -> "dept_code";
            default -> throw new IllegalArgumentException("Invalid sort column");
        };

        return jdbc.query(
                "SELECT employee_id, first_name, last_name, dept_code " +
                        "FROM employees WHERE last_name LIKE ? ORDER BY " + safeSort,
                (rs, rowNum) -> new employeeSummary(
                        rs.getString("employee_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("dept_code")),
                prefix + "%");
    }


    public List<Employee> findById(String id) {
        return jdbc.query(
                "SELECT * " + "FROM employees WHERE employee_id = ?",
                mapper,
                id);
    }

    public void updateBankAccount(String id, String account) {
        jdbc.update("UPDATE employees SET bank_account = ? WHERE employee_id = ?", crypto.encrypt(account), id);
    }

    public void migrateSensitiveFields() {
        record StoredFields(String id, String ssn, String bankAccount) {
        }

        List<StoredFields> rows = jdbc.query(
                "SELECT employee_id, ssn, bank_account " +
                        "FROM employees ORDER BY employee_id FOR UPDATE",
                (rs, rowNum) -> new StoredFields(
                        rs.getString("employee_id"),
                        rs.getString("ssn"),
                        rs.getString("bank_account")));

        for (StoredFields row : rows) {
            String ssn = row.ssn();
            String bank = row.bankAccount();

            boolean ssnDone = ssn.startsWith("gcm:");
            boolean bankDone = bank.startsWith("gcm:");

            if (ssnDone) {
                crypto.decrypt(ssn);
            } else {
                ssn = crypto.encrypt(ssn);
            }

            if (bankDone) {
                crypto.decrypt(bank);
            } else {
                bank = crypto.encrypt(crypto.decryptLegacy(bank));
            }

            if (!ssnDone || !bankDone) {
                jdbc.update(
                        "UPDATE employees SET ssn = ?, bank_account = ? " +
                                "WHERE employee_id = ?",
                        ssn, bank, row.id());
            }
        }
    }

    public String findStoredPin(String employeeId) {
        List<String> pins = jdbc.query(
                "SELECT portal_pin FROM employees WHERE employee_id = ?",
                (rs, rowNum) -> rs.getString("portal_pin"),
                employeeId
        );

        return pins.isEmpty() ? null : pins.get(0);
    }

    public void migratePins() {
        record StoredPin(String id, String value) {}

        List<StoredPin> rows = jdbc.query(
                "SELECT employee_id, portal_pin FROM employees "
                        + "ORDER BY employee_id FOR UPDATE",
                (rs, rowNum) -> new StoredPin(
                        rs.getString("employee_id"),
                        rs.getString("portal_pin"))
        );
        int processed = 0;
        long started = System.nanoTime();
        for (StoredPin row : rows) {
            String stored = row.value();

            if (stored.startsWith("pbkdf2:")) {
                // Validate the existing hash without changing it.
                pinHasher.verify("000000", stored);
            } else {
                String hashed = pinHasher.hash(
                        crypto.decryptLegacy(stored));

                jdbc.update(
                        "UPDATE employees SET portal_pin = ? "
                                + "WHERE employee_id = ?",
                        hashed, row.id()
                );
            }

            processed++;

            if (processed == 1 || processed % 50 == 0) {
                long seconds =
                        (System.nanoTime() - started) / 1_000_000_000;

                System.out.println(
                        "PIN migration: " + processed + "/" + rows.size()
                                + " processed in " + seconds + " seconds");
            }
        }
    }

}
