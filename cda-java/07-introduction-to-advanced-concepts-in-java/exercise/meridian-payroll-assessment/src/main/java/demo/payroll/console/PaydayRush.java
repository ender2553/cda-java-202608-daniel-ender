package demo.payroll.console;

import demo.payroll.domain.FundingCheck;
import demo.payroll.domain.PayRunResult;
import demo.payroll.service.PayrollService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Runs every department's payroll at once, then checks the totals. */
@Component
public class PaydayRush {

    private static final List<String> DEPARTMENTS = List.of("ENG", "OPS", "FIN", "HRS", "SAL", "MKT", "SUP", "LEG");

    private final PayrollService payroll;

    public PaydayRush(PayrollService payroll) {
        this.payroll = payroll;
    }

    public void run(LocalDate payDate) {
        CountDownLatch go = new CountDownLatch(1);
        List<Future<String>> outcomes = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(DEPARTMENTS.size())) {
            for (String dept : DEPARTMENTS) {
                outcomes.add(pool.submit(() -> {
                    go.await();
                    return runOne(dept, payDate);
                }));
            }
            go.countDown();
            for (Future<String> outcome : outcomes) {
                System.out.println(outcome.get());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("Payday rush interrupted.");
        } catch (ExecutionException ex) {
            System.out.println("Payday rush could not finish.");
        }
        FundingCheck check = payroll.checkFunding();
        System.out.printf("Funding check: opening %s, current %s, stubs %s -> %s%n",
                check.opening(), check.current(), check.stubTotal(), check.balanced() ? "BALANCED" : "OUT OF BALANCE");
    }

    private String runOne(String dept, LocalDate payDate) {
        try {
            PayRunResult result = payroll.runPayroll(dept, payDate);
            return dept + ": " + result.stubs() + " stubs, " + result.total();
        } catch (RuntimeException ex) {
            return dept + ": failed - " + ex.getMessage();
        }
    }
}
