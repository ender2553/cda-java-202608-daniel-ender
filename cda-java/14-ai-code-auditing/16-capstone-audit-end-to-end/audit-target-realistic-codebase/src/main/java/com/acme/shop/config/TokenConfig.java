package com.acme.shop.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenConfig {

    // Signing key for session tokens issued by the order service.
    public static final String JWT_SIGNING_KEY = "s3cr3t-shop-signing-key-do-not-share";

    public String signingKey() {
        return JWT_SIGNING_KEY;
    }
}
