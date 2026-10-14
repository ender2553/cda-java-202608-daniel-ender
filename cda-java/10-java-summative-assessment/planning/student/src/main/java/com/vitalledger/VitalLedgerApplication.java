package com.vitalledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VitalLedger — secure clinical-records backend (instructor reference solution).
 *
 * <p>Demonstrates the Cyber Developer Associate secure-development competencies:
 * AES-GCM encryption at rest, a parameterized secure repository, a fail-closed
 * validating service tier, an RBAC-protected REST API with non-leaking error
 * handling, and a PHI-minimizing / prompt-injection-hardened LLM summarizer.</p>
 */
@SpringBootApplication
public class VitalLedgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VitalLedgerApplication.class, args);
    }
}
