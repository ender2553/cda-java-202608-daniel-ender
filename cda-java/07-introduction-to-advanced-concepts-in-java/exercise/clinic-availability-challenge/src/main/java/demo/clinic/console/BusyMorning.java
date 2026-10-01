package demo.clinic.console;

import demo.clinic.log.CheckInCounter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Menu 8: six front desks recording check-ins at the same moment. */
final class BusyMorning {

    private static final int DESKS = 6;
    private static final int CHECK_INS_PER_DESK = 50_000;

    private BusyMorning() {
    }

    static void run(CheckInCounter counter) {
        long before = counter.count();
        CountDownLatch doorsOpen = new CountDownLatch(1);
        try (ExecutorService desks = Executors.newFixedThreadPool(DESKS)) {
            for (int d = 0; d < DESKS; d++) {
                desks.submit(() -> {
                    doorsOpen.await();
                    for (int i = 0; i < CHECK_INS_PER_DESK; i++) {
                        counter.recordCheckIn();
                    }
                    return null;
                });
            }
            doorsOpen.countDown();
        }
        long recorded = counter.count() - before;
        System.out.printf("%d desks checked in %,d patients each.%n", DESKS, CHECK_INS_PER_DESK);
        System.out.printf("Check-ins recorded this morning: %,d%n", recorded);
    }
}
