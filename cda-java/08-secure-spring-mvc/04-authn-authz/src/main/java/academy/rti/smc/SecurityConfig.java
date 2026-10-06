package academy.rti.smc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * STARTER security configuration — the control is ABSENT (insecure baseline).
 *
 * <p>This explicit {@link SecurityFilterChain} deliberately does NOT rely on
 * Spring Security auto-config defaults. As written it opens every endpoint to
 * everyone, so anonymous and under-privileged callers can both POST a
 * transaction. That makes the auth/authz assertions in the test FAIL (RED).</p>
 *
 * <p>The carried-forward controls (secure transport headers + scoped CORS) and
 * Bean Validation are already in place and are not the focus of this lesson.</p>
 *
 * <p>// TODO: require authentication + hasRole('TRANSACTOR') on
 * //       POST /api/transactions; keep GET /api/health public.
 * //       (Replace the permitAll() baseline below with deny-by-default,
 * //        least-privilege authorization rules.)</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Carried-forward control: secure transport / CORS (deliberately scoped, not the focus).
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Carried-forward control: security response headers.
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                .frameOptions(frame -> frame.deny())
            )
            // TODO: require authentication + hasRole('TRANSACTOR') on /api/transactions; keep /api/health public.
            // INSECURE BASELINE — everything is open to everyone. No authentication, no role check.
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // HTTP Basic is wired so credentials can be supplied, but the rules above
            // never actually require them.
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails viewer = User.withUsername("viewer")
                .password(encoder.encode("viewer-pass"))
                .roles("VIEWER")
                .build();
        UserDetails transactor = User.withUsername("transactor")
                .password(encoder.encode("transactor-pass"))
                .roles("TRANSACTOR")
                .build();
        return new InMemoryUserDetailsManager(viewer, transactor);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://app.rti.academy"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
