package academy.rti.smc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
 * SOLUTION security configuration — the control is PRESENT.
 *
 * <p>This lesson's control: authentication + role-based authorization with a
 * deny-by-default posture and least privilege. Only TRANSACTOR may POST a
 * transaction; the health probe stays public.</p>
 *
 * <p>Carried-forward controls (not the focus): secure transport — explicit
 * security response headers and a deliberately scoped CORS policy. Bean
 * Validation lives on {@link TransactionRequest}.</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Carried-forward control: secure transport / CORS (deliberately scoped, not the focus).
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Stateless API using HTTP Basic; CSRF is not applicable to a non-cookie credential flow.
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Carried-forward control: security response headers.
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                .frameOptions(frame -> frame.deny())
            )
            // THIS LESSON'S CONTROL: deny-by-default authorization, least privilege.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/transactions").hasRole("TRANSACTOR")
                .anyRequest().authenticated()
            )
            // HTTP Basic for test simplicity.
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
