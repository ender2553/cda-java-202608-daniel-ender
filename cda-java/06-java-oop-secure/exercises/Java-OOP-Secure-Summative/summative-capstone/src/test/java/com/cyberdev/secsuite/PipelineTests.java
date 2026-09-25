package com.cyberdev.secsuite;

import com.cyberdev.secsuite.exception.AuthenticationException;
import com.cyberdev.secsuite.testkit.GradedTest;
import com.cyberdev.secsuite.ui.ConsoleUI;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * SEC-17: Main.run -- the composition root, driven end to end with scripted login answers
 * (a ConsoleUI over a StringReader, exactly what main() builds over System.in) and a report
 * path in a temporary directory, so the real reports/ artifact is never overwritten by a test.
 *
 * This is the capstone's integration test: it only passes when EVERY other checkpoint works,
 * so expect it to be the last test to turn green. Run from the module root (Maven's default)
 * so data/threat-intel-feed.csv resolves.
 */
public class PipelineTests {

    private static final String CLOCK_PROPERTY = "secsuite.clock";
    private static final String REGISTER_AND_LOGIN =
            "y\nanalyst1\nCorrect-Horse-42\nanalyst1@example.com\nanalyst1\nCorrect-Horse-42\n";

    @Test
    @GradedTest(tag = "SEC-17", points = 2, description = "run() wires every repository and service, logs in, runs the pipeline and writes the report to the given path")
    public void run_wiresEverythingAndWritesTheReport() throws IOException {
        assertTrue(Files.isRegularFile(Path.of("data", "threat-intel-feed.csv")),
                "run the tests from the module root so that data/threat-intel-feed.csv resolves");
        Path out = Files.createTempDirectory("secsuite-sec17-").resolve("reports").resolve("report.md");
        String previousClock = System.getProperty(CLOCK_PROPERTY);
        System.setProperty(CLOCK_PROPERTY, "2026-09-24T09:00:00Z");
        try {
            Path written = Main.run(scripted(REGISTER_AND_LOGIN), out);

            assertTrue(Files.isRegularFile(out), "the report must be written to the reportFile parameter");
            assertNotNull(written);
            assertTrue(Files.isSameFile(out, written), "run() returns the path runPipeline wrote");
            String md = Files.readString(out, StandardCharsets.UTF_8);
            assertTrue(md.contains("- **Generated:** 2026-09-24T09:00:00Z"),
                    "every service must share the ONE Clock from resolveClock()");
            assertTrue(md.contains("- **Prepared by:** analyst1"), "the analyst who logged in prepares the report");
            assertTrue(md.contains("| 1 | threat-intel-feed.csv | 13 | 9 | 4 | 8 | 1 |"), "ingestion run 1");
            assertTrue(md.contains("| 2 | threat-intel-feed.csv | 13 | 9 | 4 | 0 | 9 |"),
                    "ingestion run 2 is an idempotent replay -- both runs must share ONE alert repository");
            assertTrue(md.contains("Total open findings: **8**"),
                    "7 seeded open findings + the one recorded by the simulated scan");
            assertTrue(md.contains("Total entries: **10**"), "8 promoted findings + 2 manual risks");
            assertTrue(md.contains("Components analysed: **8**, with known vulnerabilities: **5**"));
            assertTrue(md.contains("Active hits: **5**"),
                    "correlation sees the ingested alerts AND the new finding -- services share the same repositories");
        } finally {
            if (previousClock == null) {
                System.clearProperty(CLOCK_PROPERTY);
            } else {
                System.setProperty(CLOCK_PROPERTY, previousClock);
            }
        }
    }

    @Test
    @GradedTest(tag = "SEC-17", points = 1, description = "a failed login throws AuthenticationException out of run() and NOTHING after the gate runs")
    public void run_failedLoginRunsNothing() throws IOException {
        Path out = Files.createTempDirectory("secsuite-sec17-").resolve("reports").resolve("report.md");
        String threeBadAttempts = "n\nghost\nWrong-Password-1\nghost\nWrong-Password-2\nghost\nWrong-Password-3\n";

        assertThrows(AuthenticationException.class, () -> Main.run(scripted(threeBadAttempts), out),
                "the login failure must propagate out of run()");
        assertFalse(Files.exists(out), "no report may be produced without a successful login");
        assertFalse(Files.exists(out.getParent()), "the pipeline must not have started at all");
    }

    /** The same ConsoleUI main() builds, but reading scripted answers instead of System.in. */
    private static ConsoleUI scripted(String answers) {
        return new ConsoleUI(null, new BufferedReader(new StringReader(answers)),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }
}
