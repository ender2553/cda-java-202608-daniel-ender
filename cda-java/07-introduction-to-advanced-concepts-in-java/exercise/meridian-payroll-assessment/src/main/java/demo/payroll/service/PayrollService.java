package demo.payroll.service;

import demo.payroll.domain.FundingCheck;
import demo.payroll.domain.PayLine;
import demo.payroll.domain.PayRunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollService.class);
    private static final BigDecimal NET_RATE = new BigDecimal("0.75");

    private final JdbcClient jdbc;
    private final FailureInjector failureInjector;

    public PayrollService(JdbcClient jdbc, FailureInjector failureInjector) {
        this.jdbc = jdbc;
        this.failureInjector = failureInjector;
    }

    public PayRunResult runPayroll(String deptCode, LocalDate payDate) {
        List<PayLine> lines = jdbc.sql("SELECT employee_id, annual_salary FROM employees WHERE dept_code = '"
                        + deptCode + "' AND active ORDER BY employee_id")
                .query((rs, n) -> {
                    BigDecimal gross = rs.getBigDecimal("annual_salary").divide(new BigDecimal("26"), 2, RoundingMode.HALF_UP);
                    return new PayLine(rs.getString("employee_id"), gross, gross.multiply(NET_RATE).setScale(2, RoundingMode.HALF_UP));
                })
                .list();
        BigDecimal total = lines.stream().map(PayLine::net).reduce(BigDecimal.ZERO, BigDecimal::add);
        try {
            jdbc.sql("INSERT INTO payroll_audit (event, dept_code, detail) VALUES ('PAY_RUN_COMPLETED', :dept, :detail)")
                    .param("dept", deptCode)
                    .param("detail", lines.size() + " stubs, " + total)
                    .update();

            BigDecimal balance = jdbc.sql("SELECT balance FROM funding_accounts WHERE account_id = 'FND-0001'")
                    .query(BigDecimal.class)
                    .single();
            BigDecimal newBalance = balance.subtract(total);
            jdbc.sql("UPDATE funding_accounts SET balance = :balance WHERE account_id = 'FND-0001'")
                    .param("balance", newBalance)
                    .update();

            int written = 0;
            for (PayLine line : lines) {
                jdbc.sql("INSERT INTO pay_stubs (employee_id, pay_date, gross, net) VALUES (:emp, :date, :gross, :net)")
                        .param("emp", line.employeeId())
                        .param("date", payDate)
                        .param("gross", line.gross())
                        .param("net", line.net())
                        .update();
                failureInjector.maybeFail(++written, lines.size());
            }
        } catch (Exception e) {
            log.error("Payroll failed: " + e.getMessage());
        }
        return new PayRunResult(deptCode, payDate, lines.size(), total);
    }

    public FundingCheck checkFunding() {
        return jdbc.sql("""
                        SELECT opening_balance, balance, (SELECT COALESCE(SUM(net), 0) FROM pay_stubs) AS stub_total
                        FROM funding_accounts WHERE account_id = 'FND-0001'""")
                .query((rs, n) -> {
                    BigDecimal opening = rs.getBigDecimal("opening_balance");
                    BigDecimal current = rs.getBigDecimal("balance");
                    BigDecimal stubs = rs.getBigDecimal("stub_total");
                    return new FundingCheck(opening, current, stubs, opening.subtract(current).compareTo(stubs) == 0);
                })
                .single();
    }
}
