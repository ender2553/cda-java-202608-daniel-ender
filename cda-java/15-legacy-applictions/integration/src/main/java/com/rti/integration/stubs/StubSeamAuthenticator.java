package com.rti.integration.stubs;

import com.rti.integration.SeamAuthenticator;

/**
 * Unbuilt placeholder. Real token issue/verify is authored during the course.
 */
public class StubSeamAuthenticator implements SeamAuthenticator {

    @Override
    public String issueToken(String serviceId) {
        throw new UnsupportedOperationException("TODO: issue a signed seam token for the service");
    }

    @Override
    public boolean verify(String token) {
        throw new UnsupportedOperationException("TODO: verify the seam token");
    }
}
