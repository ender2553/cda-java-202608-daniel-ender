package demo.payroll.service;

import demo.payroll.crypto.BankCrypto;
import demo.payroll.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class DirectDepositService {

    private static final Logger log = LoggerFactory.getLogger(DirectDepositService.class);

    private final JdbcClient jdbc;
    private final BankCrypto crypto;
    private final EmployeeRepository employees;

    public DirectDepositService(JdbcClient jdbc, BankCrypto crypto, EmployeeRepository employees) {
        this.jdbc = jdbc;
        this.crypto = crypto;
        this.employees = employees;
    }

    public void changeAccount(String employeeId, String pin, String newAccount) {
        String[] row = jdbc.sql("SELECT ssn, portal_pin FROM employees WHERE employee_id = '" + employeeId + "'")
                .query((rs, n) -> new String[]{rs.getString("ssn"), rs.getString("portal_pin")})
                .optional()
                .orElse(null);
        if (row == null) {
            throw new IllegalArgumentException("No employee with ID " + employeeId);
        }
        if (!crypto.decrypt(row[1]).equals(pin)) {
            throw new IllegalArgumentException("Incorrect PIN");
        }
        employees.updateBankAccount(employeeId, newAccount);
        log.info("Updated bank account for {} (SSN {}) to {}", employeeId, row[0], newAccount);
    }
}
