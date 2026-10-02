package demo.ledger.log;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts log lines processed since startup. Spring makes ONE instance of this
 * class and shares it with every caller, so in a server it is updated by many
 * threads at once.
 *
 * <p><b>This is the completed solution.</b> See {@code TODO: (Completed) Step 7 of 7}.
 */
@Component
public class ProcessingStats {

    // TODO: (Completed) Step 7 of 7 — Make the shared counter thread-safe.
    //   - Menu 8 showed lost updates. Even on a volatile int, linesProcessed += count is three
    //     steps (read, add, write). Two threads can read the same value, and both write back
    //     value + count, so one update disappears. volatile makes each read and write visible;
    //     it does not make the three steps one. An int also overflows past about 2.1 billion.
    //   - AtomicLong does the read, add and write as one indivisible step, and holds 64 bits.
    //   - Rerun menu 8: expected and actual now match on every run.
    private final AtomicLong linesProcessed = new AtomicLong();

    public void recordLines(long count) {
        linesProcessed.addAndGet(count);
    }

    public long linesProcessed() {
        return linesProcessed.get();
    }
}
