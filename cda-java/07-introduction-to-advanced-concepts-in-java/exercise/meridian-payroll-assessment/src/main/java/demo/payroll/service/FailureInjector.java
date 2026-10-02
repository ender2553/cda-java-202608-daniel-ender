package demo.payroll.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates an outage partway through a pay run (menu 6).
 */
@Component
public class FailureInjector {

    private final AtomicBoolean enabled = new AtomicBoolean();

    /** Flips the switch and returns the new state. */
    public boolean toggle() {
        boolean was;
        do {
            was = enabled.get();
        } while (!enabled.compareAndSet(was, !was));
        return !was;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void maybeFail(int written, int total) {
        if (enabled.get() && written == total / 2) {
            throw new IllegalStateException("Simulated outage after " + written + " of " + total + " stubs");
        }
    }
}
