package demo.ledger.log;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts log lines processed since startup. Spring makes ONE instance of this
 * class and shares it with every caller, so in a server it is updated by many
 * threads at once.
 */
@Component
public class ProcessingStats {

    // TODO: Step 7 of 7 — Make the shared counter thread-safe.
    //   - Run it first: menu 8 runs 8 threads that each add 1 to this counter 250,000 times.
    //     Expected is 2,000,000. Run menu 8 three times and compare the Actual numbers.
    //   - linesProcessed += count looks like one step, but it is three: read, add, write.
    //     Two threads can read the same value, and both write back value + count.
    //   - The field is even volatile, so every thread sees the latest value. It still loses
    //     updates: volatile makes each read and each write visible, not read-add-write atomic.
    //   - Replace the int with a java.util.concurrent.atomic.AtomicLong (already imported):
    //     a final field set to new AtomicLong(), addAndGet(count) in recordLines, and get()
    //     in linesProcessed. Rerun menu 8 until you trust the result.
    //   - Bonus question: what else goes wrong with an int once this counts 3,000,000 lines
    //     per summary, for a few hundred summaries?
    private volatile int linesProcessed;

    public void recordLines(long count) {
        linesProcessed += count;
    }

    public long linesProcessed() {
        return linesProcessed;
    }
}
