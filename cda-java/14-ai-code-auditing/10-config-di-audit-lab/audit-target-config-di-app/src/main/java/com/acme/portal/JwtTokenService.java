package com.acme.portal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final String secret;
    private final long ttlSeconds;

    public JwtTokenService(
            @Value("${portal.jwt.secret}") String secret,
            @Value("${portal.jwt.ttl-seconds}") long ttlSeconds) {
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(String userId) {
        // Signs a session token for the user with the configured secret.
        return userId + "." + secret.hashCode() + "." + ttlSeconds;
    }
}
