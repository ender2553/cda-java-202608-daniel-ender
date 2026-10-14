package com.vitalledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.vitalledger.crypto.AesGcmCipher;
import com.vitalledger.crypto.EncryptionKeyProvider;

import org.junit.jupiter.api.Test;

/**
 * Starter test harness for VitalLedger.
 *
 * <p>This is intentionally minimal. The full security test suite is <b>your</b> work:
 * in the testing phase you author the tests that prove encryption at rest, fail-closed
 * validation, transactional all-or-nothing writes, RBAC enforcement, no-leak error
 * handling, and the PHI-minimizing / prompt-injection-hardened summarizer. Treat those
 * tests as evidence for the capstone rubric.</p>
 *
 * <h2>How to run the tests</h2>
 * <pre>
 *   # Use a Java 21 toolchain
 *   export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS
 *
 *   mvn test            # compile + run the test suite
 *   mvn -q clean verify # full clean build
 * </pre>
 *
 * <h2>How to build and run the app</h2>
 * <pre>
 *   # 1. Provide the required environment configuration
 *   cp .env.example .env
 *   #    then set VITALLEDGER_AES_KEY (base64 AES key):  openssl rand -base64 32
 *
 *   # 2. Run locally
 *   VITALLEDGER_AES_KEY=$(openssl rand -base64 32) mvn spring-boot:run
 *
 *   # 3. Or with Docker
 *   docker compose up --build
 * </pre>
 *
 * <p><b>Expect RED until you build it.</b> The example below exercises the encryption
 * layer and will pass only after you implement {@code AesGcmCipher} in the
 * data + encryption phase. Skeleton method bodies throw
 * {@link UnsupportedOperationException} with a {@code TODO(phase X)} marker until then.</p>
 */
class ExampleSmokeTest {

    /** Sanity check that the test harness itself runs. This should always pass. */
    @Test
    void harnessRuns() {
        assertEquals(2, 1 + 1, "JUnit harness is wired up");
    }

    /**
     * Example of the kind of unit test you will write. It builds the cipher from the
     * test-only key in {@code src/test/resources/application.properties} and asserts a
     * round-trip. It is RED against the skeleton and turns GREEN once you implement
     * AES-GCM encrypt/decrypt in the data + encryption phase.
     */
    @Test
    void encryptionRoundTripExample_passesOncePhaseBIsImplemented() {
        // Test-only synthetic AES-256 key (base64) — NOT a real secret.
        EncryptionKeyProvider keyProvider =
                new EncryptionKeyProvider("ciT4RD8Uk3GXdiRtYRBYsIoSgHFVQ5uDCrZVN3ZGGX4=");
        AesGcmCipher cipher = new AesGcmCipher(keyProvider);

        String plaintext = "clinical note: patient stable";
        String encrypted = cipher.encrypt(plaintext);

        assertNotNull(encrypted, "ciphertext should not be null");
        assertEquals(plaintext, cipher.decrypt(encrypted), "decrypt(encrypt(x)) == x");
    }
}
