package demo.payroll.service;

import demo.payroll.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MigrationService {

    private final EmployeeRepository employees;

    public MigrationService(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Transactional
    public void migrate() {
        employees.migrateSensitiveFields();
        employees.migratePins();
    }
}