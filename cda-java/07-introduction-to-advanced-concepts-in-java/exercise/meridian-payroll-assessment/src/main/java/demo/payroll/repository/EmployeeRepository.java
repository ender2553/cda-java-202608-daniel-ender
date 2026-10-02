package demo.payroll.repository;

import demo.payroll.crypto.BankCrypto;
import demo.payroll.domain.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbc;
    private final BankCrypto crypto;
    private final RowMapper<Employee> mapper;

    public EmployeeRepository(JdbcTemplate jdbc, BankCrypto crypto) {
        this.jdbc = jdbc;
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
                crypto.decrypt(rs.getString("portal_pin")),
                rs.getString("hr_notes"));
    }

    public List<Employee> search(String prefix, String sortColumn) {
        return jdbc.query("SELECT * FROM employees WHERE last_name LIKE '" + prefix + "%' ORDER BY " + sortColumn, mapper);
    }

    public List<Employee> findById(String id) {
        return jdbc.query("SELECT * FROM employees WHERE employee_id = '" + id + "'", mapper);
    }

    public void updateBankAccount(String id, String account) {
        jdbc.update("UPDATE employees SET bank_account = ? WHERE employee_id = ?", crypto.encrypt(account), id);
    }
}
