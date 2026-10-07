package academy.rti.smc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // In-memory users. These already exist so HTTP Basic *could* authenticate,
    // but the filter chain below never requires it.
    @Bean
    public InMemoryUserDetailsManager users(PasswordEncoder encoder) {
        UserDetails viewer = User.withUsername("viewer")
                .password(encoder.encode("viewer-pw"))
                .roles("VIEWER")
                .build();
        UserDetails transactor = User.withUsername("transactor")
                .password(encoder.encode("transactor-pw"))
                .roles("TRANSACTOR")
                .build();
        return new InMemoryUserDetailsManager(viewer, transactor);
    }

    // STARTER FILTER CHAIN — WIDE OPEN.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // TODO (Authorization): replace permitAll with rule-based access:
                //      - GET  /api/health           -> permitAll
                //      - POST /api/transactions      -> hasRole("TRANSACTOR")
                //      - anything else               -> authenticated
                //   As written everything is open, so:
                //      test #2 (anonymous POST -> 401) FAILS
                //      test #3 (viewer POST    -> 403) FAILS
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // TODO (Authentication): enable HTTP Basic so credentials are
                //      actually checked: .httpBasic(Customizer.withDefaults())

                // TODO (CSRF stance): this is a stateless JSON API authenticated
                //      per-request with HTTP Basic, so disable CSRF deliberately:
                //      .csrf(csrf -> csrf.disable())
                //   (left at defaults here; harmless for the tests but not a
                //    deliberate, documented stance)
                .csrf(csrf -> csrf.disable())

                // TODO (Secure transport - headers): security headers are at
                //      framework defaults. Test #7 asserts X-Content-Type-Options
                //      is present; Spring sets nosniff by default so #7 may pass,
                //      but the deliberate hardening below is missing:
                //      .headers(h -> h
                //          .contentTypeOptions(Customizer.withDefaults())
                //          .frameOptions(f -> f.deny())
                //          .httpStrictTransportSecurity(Customizer.withDefaults()))

                // TODO (Secure transport - least-privilege CORS): no CORS config.
                //      Test #8 (allow https://app.example.com, deny
                //      https://evil.example.com) FAILS because nothing is allowed.
                //      Add: .cors(Customizer.withDefaults()) plus a
                //      CorsConfigurationSource bean allowing only app.example.com.
                ;

        return http.build();
    }
}
