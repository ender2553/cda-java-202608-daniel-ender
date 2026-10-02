package demo.payroll.domain;

import java.math.BigDecimal;

public record Employee(String employeeId, String firstName, String lastName, String deptCode, String title,
                       BigDecimal annualSalary, boolean active, String ssn, String bankAccount, String portalPin,
                       String hrNotes) {
    @Override
    public String toString() {
        return "Employee[employeeId=" + employeeId
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + ", deptCode=" + deptCode
                + ", title=" + title
                + ", active=" + active + "]";
    }
}