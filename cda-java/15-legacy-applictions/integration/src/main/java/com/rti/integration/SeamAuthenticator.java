package com.rti.integration;

/**
 * Issues and verifies tokens for calls that cross the seam into the monolith.
 */
public interface SeamAuthenticator {

    String issueToken(String serviceId);

    boolean verify(String token);
}
