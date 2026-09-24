package com.example.paintcalc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {
    /** SECURITY: BCrypt is slow and salted; the work factor should be benchmarked for production. */
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
}
