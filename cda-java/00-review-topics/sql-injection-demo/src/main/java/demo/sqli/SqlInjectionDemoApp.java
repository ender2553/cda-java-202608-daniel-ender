package demo.sqli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the Spring MVC web app on http://localhost:8080.
 *
 * <p>This is a teaching sandbox. It is DELIBERATELY insecure — the product
 * search builds SQL by string concatenation — so SQL injection can be shown
 * and discussed against a throwaway database that holds only fake data.
 */
@SpringBootApplication
public class SqlInjectionDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(SqlInjectionDemoApp.class, args);
    }
}
