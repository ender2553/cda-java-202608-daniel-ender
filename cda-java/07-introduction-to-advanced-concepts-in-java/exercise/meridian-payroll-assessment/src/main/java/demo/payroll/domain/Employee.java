package demo.payroll.domain;

import java.math.BigDecimal;

public record Employee(String employeeId, String firstName, String lastName, String deptCode, String title,
                       BigDecimal annualSalary, boolean active, String ssn, String bankAccount, String portalPin,
                       String hrNotes) {
}
