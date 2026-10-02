package demo.clinic.log;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/** Today's check-in count, shared by every front desk. */
@Component
public class CheckInCounter {

    private final AtomicLong checkIns = new AtomicLong();

    public void recordCheckIn() {
        checkIns.incrementAndGet();
    }

    public long count() {
        return checkIns.get();
    }
}
