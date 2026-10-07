package academy.rti.smc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Pre-baked prior control: authentication/authorization wiring (Spring Security).
 *
 * <p>For THIS lab the focus is error-body safety, so the API is permitted to all and
 * CSRF is disabled — this isolates the error-handling assertions from auth concerns.
 * In the secure-transport / authn lessons this config carried HTTPS + authentication;
 * here it is deliberately simplified to {@code permitAll}.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
