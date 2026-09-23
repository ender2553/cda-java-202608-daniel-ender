package com.cybersoft.vehicle.config;

import com.cybersoft.vehicle.crypto.FieldEncryptor;
import com.cybersoft.vehicle.crypto.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordHasher passwordHasher() {
        return new PasswordHasher();
    }

    @Bean
    public FieldEncryptor fieldEncryptor() {
        byte[] demoKey = new byte[32];
        new SecureRandom().nextBytes(demoKey);
        try {
            return new FieldEncryptor(demoKey);
        } finally {
            Arrays.fill(demoKey, (byte) 0);
        }
    }
}
