package academy.rti.smc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Secure Method Coding Lab 05 - Secure Error Handling.
 *
 * <p>Theme: error responses must never leak stack traces, exception class names,
 * or internal detail (DB connection strings, secrets). Full detail is logged
 * server-side; clients receive a safe RFC 7807 {@code application/problem+json} body.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
