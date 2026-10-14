package com.vitalledger.security;

import com.vitalledger.web.RestAccessDeniedHandler;
import com.vitalledger.web.RestAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central security policy.
 *
 * <ul>
 *   <li><b>Authentication</b>: HTTP Basic against the encrypted user store; BCrypt hashes.</li>
 *   <li><b>RBAC</b>: method-level {@code @PreAuthorize} on controllers (enabled here),
 *       plus URL rules as defense in depth.</li>
 *   <li><b>Secure headers</b>: HSTS, X-Content-Type-Options, frame-deny, CSP, referrer policy.</li>
 *   <li><b>Stateless</b>: no session, no CSRF token flow for this token-less API; every
 *       request re-authenticates.</li>
 *   <li><b>No-leak auth failures</b>: custom 401/403 entry points return RFC-7807 bodies.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RestAuthenticationEntryPoint authEntryPoint,
                                           RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        // TODO(phase D): configure the stateless HTTP Basic filter chain — disable CSRF, require auth on /api/**,
        // wire the custom 401/403 entry points, and set secure response headers (HSTS, X-Content-Type-Options,
        // frame-deny, CSP, referrer policy). Fine-grained RBAC is enforced by @PreAuthorize on controllers.
        throw new UnsupportedOperationException("TODO(phase D)");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
