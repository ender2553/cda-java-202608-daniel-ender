package demo.payroll.domain;

public record employeeSummary(
        String employeeId,
        String firstName,
        String lastName,
        String deptCode) {
}