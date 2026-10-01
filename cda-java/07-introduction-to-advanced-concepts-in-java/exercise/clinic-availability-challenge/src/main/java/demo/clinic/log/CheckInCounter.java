package demo.clinic.log;

import org.springframework.stereotype.Component;

/** Today's check-in count, shared by every front desk. */
@Component
public class CheckInCounter {

    private volatile int checkIns;

    public void recordCheckIn() {
        checkIns++;
    }

    public long count() {
        return checkIns;
    }
}
