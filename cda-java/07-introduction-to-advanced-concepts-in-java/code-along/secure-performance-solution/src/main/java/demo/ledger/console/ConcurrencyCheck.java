package demo.ledger.console;

import demo.ledger.log.ProcessingStats;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Menu 8: many callers updating one shared {@link ProcessingStats} at the same
 * moment, the way request threads share a Spring singleton in a server.
 * Single-threaded testing can't show a lost update; this can.
 */
final class ConcurrencyCheck {

    private static final int THREADS = 8;
    private static final int UPDATES_PER_THREAD = 250_000;

    private ConcurrencyCheck() {
    }

    static void run() {
        // A fresh instance, so this check doesn't change the app's own counts.
        ProcessingStats shared = new ProcessingStats();
        CountDownLatch startTogether = new CountDownLatch(1);

        // ExecutorService is AutoCloseable (Java 19+): close() waits for every task to finish.
        try (ExecutorService pool = Executors.newFixedThreadPool(THREADS)) {
            for (int t = 0; t < THREADS; t++) {
                pool.submit(() -> {
                    startTogether.await();
                    for (int i = 0; i < UPDATES_PER_THREAD; i++) {
                        shared.recordLines(1);
                    }
                    return null;
                });
            }
            startTogether.countDown();
        }

        long expected = (long) THREADS * UPDATES_PER_THREAD;
        long actual = shared.linesProcessed();
        System.out.printf("%d threads x %,d updates%n", THREADS, UPDATES_PER_THREAD);
        System.out.printf("Expected: %,d%n", expected);
        System.out.printf("Actual:   %,d%n", actual);
        System.out.println(actual == expected
                ? "OK: no updates were lost."
                : String.format("LOST %,d updates.", expected - actual));
    }
}
