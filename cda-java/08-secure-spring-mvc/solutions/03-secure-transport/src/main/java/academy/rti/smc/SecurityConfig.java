package academy.rti.smc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SOLUTION SecurityConfig — transport-layer security.
 *
 * Authentication is OUT OF SCOPE (a LATER lab): every request is permitted. The
 * control added by THIS lesson is transport security on top of that permissive
 * authorization:
 *
 *   - Security headers: Spring Security's defaults (incl. X-Content-Type-Options:
 *     nosniff) PLUS an explicitly-configured Referrer-Policy: no-referrer.
 *   - Least-privilege CORS: an allow-list of exactly https://app.example.com.
 *     The allowed origin is echoed back on preflight; any other origin (e.g.
 *     https://evil.example.com) is not granted CORS access.
 *   - CSRF: disabled deliberately because this is a stateless JSON API with no
 *     cookie/session-based auth to protect (see README for the rationale).
 */
@Configuration
public class SecurityConfig {

    private static final String ALLOWED_ORIGIN = "https://app.example.com";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Stateless JSON API: no server-side session.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF is not applicable to a stateless, token-less JSON API.
                .csrf(csrf -> csrf.disable())
                // Wire the least-privilege CORS allow-list below.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Add the required Referrer-Policy header on top of the secure defaults
                // (the defaults already include X-Content-Type-Options: nosniff).
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                // Authentication is a later lab; permit everything for now.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(ALLOWED_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
