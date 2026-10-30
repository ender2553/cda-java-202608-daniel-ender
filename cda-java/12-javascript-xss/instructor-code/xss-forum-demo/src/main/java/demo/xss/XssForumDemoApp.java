package demo.xss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the forum on http://localhost:8080.
 *
 * <p>This is a teaching sandbox. It is DELIBERATELY insecure: comment text is
 * stored and rendered without escaping, so stored XSS can be shown and
 * discussed against a throwaway database that holds only fake data.
 */
@SpringBootApplication
public class XssForumDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(XssForumDemoApp.class, args);
    }
}
