package org.example;
/*
 * ============================================================
 * DAY 7 AFTERNOON LAB — RiskRegisterTest.java (YOU FILL THIS IN)
 * ============================================================
 *
 * Each test below has a name, a signature, and a comment describing
 * what it must verify — but no body yet. Fill in the Arrange-Act-
 * Assert for each one. A stub currently fails on purpose
 * (fail("TODO: ...")) so you always know which tests are still
 * incomplete when you run the suite — don't just delete the fail()
 * call without replacing it with real assertions.
 *
 * You may add additional test methods beyond the ones listed here if
 * you think of more edge cases worth covering.
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RiskRegisterTest {

    private RiskRegister register;

    @BeforeEach
    void setUp() {
        register = new RiskRegister();
    }

    // Given — a small convenience so every test isn't repeating the
    // same six-argument constructor call. Feel free to add more
    // helpers like this of your own if it makes your tests cleaner.
    private Vulnerability sampleFinding(String cveId) {
        return new Vulnerability(cveId, "openssl", "3.0.1", Severity.CRITICAL,
            new BigDecimal("150.00"), LocalDate.of(2024, 1, 15));
    }

    // ============================================================
    // HAPPY PATH
    // ============================================================

    @Test
    void addingAValidFindingSucceeds() {
        // Arrange: create one valid Vulnerability (sampleFinding() will help).
        // Act: add it to the register.
        // Assert: getFindings() has size 1, and getUniqueCveIds() contains its CVE ID.
        fail("TODO: implement addingAValidFindingSucceeds");
    }

    @Test
    void saveAndLoadRoundTripPreservesFindings(@TempDir Path tempDir) throws Exception {
        // Arrange: add two or more findings to the register.
        // Act: saveToFile() to a path inside tempDir, then RiskRegister.loadFromFile() it back.
        // Assert: the loaded register has the same number of findings and the same
        //         set of unique CVE IDs as the original.
        fail("TODO: implement saveAndLoadRoundTripPreservesFindings");
    }

    // ============================================================
    // BOUNDARY-VALUE ANALYSIS — the CVE ID sequence-number length
    //
    // A CVE ID's sequence number must be 4 to 7 digits. The
    // interesting values are AT and JUST PAST that boundary, not
    // comfortably in the middle of it.
    // ============================================================

    @Test
    void cveIdWithSevenDigitSequenceIsAccepted() {
        // Assert: addFinding() with a CVE ID like "CVE-2024-1234567"
        // (exactly 7 digits) does NOT throw. (Hint: assertDoesNotThrow)
        fail("TODO: implement cveIdWithSevenDigitSequenceIsAccepted");
    }

    @Test
    void cveIdWithEightDigitSequenceIsRejected() {
        // Assert: addFinding() with a CVE ID like "CVE-2024-12345678"
        // (8 digits — one past the maximum) throws IllegalArgumentException.
        fail("TODO: implement cveIdWithEightDigitSequenceIsRejected");
    }

    // ============================================================
    // ABUSE CASES — CVE ID and component validation
    //
    // Cover this week's edge-case catalog: null, empty, malformed,
    // and (for component) injection-shaped input that could corrupt
    // saveToFile()'s pipe-delimited format. Consider converting either
    // of these to a @ParameterizedTest with @NullSource, @EmptySource,
    // and @ValueSource once you have the individual cases working —
    // see this week's Secure TDD material for the pattern.
    // ============================================================

    @Test
    void rejectsInvalidCveIds() {
        // Assert: RiskRegister.validateCveId(...) throws IllegalArgumentException
        // for AT LEAST: null, "", and a string that doesn't match CVE-YYYY-NNNN
        // (e.g. "not-a-cve").
        fail("TODO: implement rejectsInvalidCveIds");
    }

    @Test
    void rejectsInvalidComponents() {
        // Assert: RiskRegister.validateComponent(...) throws IllegalArgumentException
        // for AT LEAST: null, "", a component over 50 characters, a component
        // containing '|', and a component containing a newline.
        fail("TODO: implement rejectsInvalidComponents");
    }

    @Test
    void addingADuplicateCveIdThrowsDuplicateFindingException() {
        // Arrange: add a finding with a given CVE ID.
        // Assert: adding ANOTHER finding with the SAME CVE ID throws
        // DuplicateFindingException.
        fail("TODO: implement addingADuplicateCveIdThrowsDuplicateFindingException");
    }

    @Test
    void getFindingsReturnsAViewThatCannotBeMutatedExternally() {
        // Arrange: add a finding.
        // Assert: calling .add(...) on the List returned by getFindings()
        // throws UnsupportedOperationException.
        fail("TODO: implement getFindingsReturnsAViewThatCannotBeMutatedExternally");
    }

    // ============================================================
    // FILE I/O ABUSE CASES
    // ============================================================

    @Test
    void loadingAMissingFileThrowsIOException() {
        // Assert: RiskRegister.loadFromFile() on a filename that doesn't
        // exist throws IOException.
        fail("TODO: implement loadingAMissingFileThrowsIOException");
    }

    @Test
    void loadingACorruptedFileThrowsInvalidRegisterDataException(@TempDir Path tempDir) throws IOException {
        // Arrange: write a file inside tempDir with a line that does NOT
        // split into exactly 6 pipe-delimited fields (e.g. only 4).
        // Assert: RiskRegister.loadFromFile() throws InvalidRegisterDataException.
        fail("TODO: implement loadingACorruptedFileThrowsInvalidRegisterDataException");
    }

    // ============================================================
    // DEBUGGING REGRESSION TEST
    //
    // Write this AFTER you've investigated LegacyScanOutputParser.java.
    // It should fail against the ORIGINAL parser and pass once the
    // parser is fixed — that's how you prove your fix actually works,
    // not just that you changed something.
    // ============================================================

    @Test
    void importLegacyScanResultsRejectsALineWithExtraTrailingFields(@TempDir Path tempDir) throws IOException {
        // Arrange: write a legacy-format file inside tempDir with a line
        // that has MORE than 4 comma-separated fields, e.g.:
        //   "CVE-2023-5555,log4j-core,2.14.1,H,unexpected,extra"
        // Assert: register.importLegacyScanResults(...) throws
        // InvalidRegisterDataException for that file — it should NOT be
        // silently accepted.
        fail("TODO: implement importLegacyScanResultsRejectsALineWithExtraTrailingFields");
    }
}
