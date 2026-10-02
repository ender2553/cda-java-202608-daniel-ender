package demo.payroll.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PayRunResult(String deptCode, LocalDate payDate, int stubs, BigDecimal total) {
}
