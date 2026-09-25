package com.example.rpg.config;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
/** Security dependencies are centralized so services depend on the PasswordEncoder abstraction. */
@Configuration public class SecurityConfig {
    /**
     * BCrypt is intentionally slow and salted. Work factor 12 is a teaching/demo default;
     * benchmark it on the deployment hardware and tune it for the real application.
     */
    @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
}
