package demo.payroll.domain;

import java.math.BigDecimal;

/** What one active employee is paid in one run. */
public record PayLine(String employeeId, BigDecimal gross, BigDecimal net) {
}
