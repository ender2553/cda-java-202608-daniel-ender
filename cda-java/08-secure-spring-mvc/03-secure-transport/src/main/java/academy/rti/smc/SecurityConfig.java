package academy.rti.smc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * STARTER (insecure baseline) SecurityConfig.
 *
 * Authentication is OUT OF SCOPE for this lesson (it is a LATER lab), so every
 * request is permitted. CSRF is disabled because this is a stateless JSON API.
 *
 * What is MISSING (your job to add):
 *   - The required transport security header(s): an explicit Referrer-Policy.
 *   - A least-privilege CORS allow-list (echo https://app.example.com, reject others).
 *
 * As shipped, the filter chain does not register a CorsConfigurationSource and does
 * not add the required Referrer-Policy header, so the transport assertions in
 * SecureTransportTest FAIL (RED). Make them pass by configuring the filter chain
 * in the instructor solution shape.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // TODO: configure security headers + least-privilege CORS
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // Add the required Referrer-Policy header.
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer
                                .policy(
                                        org.springframework.security.web.header.writers
                                                .ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                                )
                        )
                )

                // Enable the CORS configuration defined below.
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow exactly the trusted frontend.
        configuration.setAllowedOrigins(
                List.of("https://app.example.com")
        );

        // Methods required by the API.
        configuration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name()
                )
        );

        // JSON requests use Content-Type.
        configuration.setAllowedHeaders(
                List.of("Content-Type")
        );

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}

