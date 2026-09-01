package org.example;
/*
 * ============================================================
 * INDIVIDUAL GRADED LAB — Day 6
 * Secure Alert Intake Toolkit
 * FILE 4 of 7: AlertFeedConnection.java
 * ============================================================
 *
 * This class stands in for a real feed connection (a network socket,
 * a message queue subscription) so you get real practice with
 * try-with-resources without needing actual I/O.
 *
 * TODO 1: Make this class "implements AutoCloseable".  //Example: ... <class name> implements AutoCloseable
 *
 * TODO 2: Add a private final String field for the feed's name, and a
 * constructor that sets it and prints "Opened feed connection: <name>".
 *
 * TODO 3: Write fetchRawLines() returning a List<String> — return
 * EXACTLY these seven lines (this specific mix is what the rest of
 * the lab is designed and graded against):
 *
 *   "CVE-2024-1234,openssl,HIGH,7.5",
 *   "CVE-2024-5678,log4j,CRITICAL,9.8",
 *   "CVE-2024-1234,openssl,HIGH,7.5",
 *   "NOT-A-CVE,curl,LOW,3.0",
 *   "CVE-2024-9999,unknown-lib,SUPER_HIGH,5.0",
 *   "CVE-2024-1111,openssl,MEDIUM,999",
 *   "CVE-2024-2222,openssl,MEDIUM,not-a-number"
 *
 * (Three valid lines — with one CVE ID deliberately repeated — plus
 * four lines, each malformed in a different way, to exercise every
 * validation path you'll build in AlertParser.)
 *
 * TODO 4: Implement close() (required by AutoCloseable) to print  //Example. ...<class name> implements AutoCloseable
 * "Closed feed connection: <name>".
 * ============================================================
 */
import java.util.List;


    // TODO 1, 2: declare "implements AutoCloseable" above, add the
    // field, and write the constructor here.

public class AlertFeedConnection implements AutoCloseable {

    private final String name;

    public AlertFeedConnection(String name) {
        this.name = name;
        System.out.println("Opened feed connection: " + name);
    }

    // TODO 3: write fetchRawLines() here.

    public List<String> fetchRawLines() {
        return List.of(
                "CVE-2024-1234,openssl,HIGH,7.5",
                "CVE-2024-5678,log4j,CRITICAL,9.8",
                "CVE-2024-1234,openssl,HIGH,7.5",
                "NOT-A-CVE,curl,LOW,3.0",
                "CVE-2024-9999,unknown-lib,SUPER_HIGH,5.0",
                "CVE-2024-1111,openssl,MEDIUM,999",
                "CVE-2024-2222,openssl,MEDIUM,not-a-number"
        );
    }

    // TODO 4: write close() here.

    @Override
    public void close() {
        System.out.println("Closed feed connection: " + name);
    }

}
