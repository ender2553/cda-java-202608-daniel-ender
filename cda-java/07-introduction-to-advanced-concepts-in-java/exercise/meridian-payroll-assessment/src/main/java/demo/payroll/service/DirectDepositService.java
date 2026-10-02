package demo.payroll.service;
import demo.payroll.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import demo.payroll.crypto.PinHasher;

@Service
public class DirectDepositService {

    private static final Logger log = LoggerFactory.getLogger(DirectDepositService.class);


    private final PinHasher pinHasher;
    private final EmployeeRepository employees;

    public DirectDepositService(PinHasher pinHasher, EmployeeRepository employees) {
        this.pinHasher = pinHasher;
        this.employees = employees;
    }

    public void changeAccount(String employeeId, String pin, String newAccount) {

        if (employeeId == null || !employeeId.matches("EMP-[0-9]{5}")) {
            throw new IllegalArgumentException("Invalid employee ID");
        }

        if (pin == null || !pin.matches("[0-9]{6}")) {
            throw new IllegalArgumentException("PIN must contain 6 digits");
        }

        if (newAccount == null || !newAccount.matches("[0-9]{8,17}")) {
            throw new IllegalArgumentException("Account must contain 8 to 17 digits");
        }
        String storedPin = employees.findStoredPin(employeeId);

        if (storedPin == null) {
            throw new IllegalArgumentException("Unable to verify employee credentials");
        }

        if (!pinHasher.verify(pin, storedPin)) {
            throw new IllegalArgumentException("Unable to verify employee credentials");
        }

        employees.updateBankAccount(employeeId, newAccount);
        log.info("Direct-deposit account updated.");
    }
}