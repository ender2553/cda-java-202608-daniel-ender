package demo.payroll.domain;

import java.math.BigDecimal;

/** Totals for a timesheet file. Hours are kept as whole hundredths. */
public record TimesheetSummary(long entries, long hundredths, long overtimeEntries, long rejected) {

    public BigDecimal totalHours() {
        return BigDecimal.valueOf(hundredths, 2);
    }
}
