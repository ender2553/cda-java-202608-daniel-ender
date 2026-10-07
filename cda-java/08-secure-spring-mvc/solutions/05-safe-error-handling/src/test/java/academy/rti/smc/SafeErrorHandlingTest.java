package academy.rti.smc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Identical in both the student (starter) and instructor (solution) trees.
 *
 * <p>Run against a real servlet container ({@code RANDOM_PORT}) so the FULL servlet
 * error-dispatch path executes — this is what makes the starter's information leak
 * concrete (the default {@code BasicErrorController} body, with stack trace + message
 * includes turned on, actually contains the leaked detail).
 *
 * <p>RED/GREEN contract:
 * <ul>
 *   <li>Student tree (no @ControllerAdvice): error bodies leak; the "no leak" /
 *       problem+json assertions FAIL.</li>
 *   <li>Instructor tree (GlobalExceptionHandler present): all assertions PASS.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SafeErrorHandlingTest {

    @Autowired
    private TestRestTemplate rest;

    /** Substrings that must NEVER appear in a client-facing error body. */
    private static final String[] FORBIDDEN_VALIDATION = {
            "Exception",
            "org.springframework",
            "MethodArgumentNotValid"
    };

    private static final String[] FORBIDDEN_INTERNAL = {
            "Exception",
            "org.springframework",
            "RuntimeException",
            "jdbc",
            "secret",
            "password",
            "at academy.rti.smc" // a stack-trace frame
    };

    @Test
    void invalidPostBody_returns400_problemJson_withNoLeak() {
        // blank accountId -> validation failure
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity =
                new HttpEntity<>("{\"accountId\":\"\",\"amount\":10.00}", headers);

        ResponseEntity<String> response =
                rest.postForEntity("/api/transactions", entity, String.class);

        assertThat(response.getStatusCode())
                .as("invalid body must return 400")
                .isEqualTo(HttpStatus.BAD_REQUEST);

        MediaType contentType = response.getHeaders().getContentType();
        String body = response.getBody() == null ? "" : response.getBody();

        assertThat(contentType)
                .as("validation error Content-Type must be application/problem+json")
                .isNotNull();
        assertThat(contentType.toString())
                .as("validation error Content-Type must be application/problem+json")
                .startsWith("application/problem+json");

        for (String forbidden : FORBIDDEN_VALIDATION) {
            assertThat(body)
                    .as("validation error body must not leak '%s'", forbidden)
                    .doesNotContain(forbidden);
        }
    }

    @Test
    void unexpectedServerError_returns500_problemJson_withGenericMessage_andNoLeak() {
        ResponseEntity<String> response =
                rest.getForEntity("/api/transactions/boom", String.class);

        assertThat(response.getStatusCode())
                .as("unexpected failure must return 500")
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        MediaType contentType = response.getHeaders().getContentType();
        String body = response.getBody() == null ? "" : response.getBody();

        assertThat(contentType)
                .as("server error Content-Type must be application/problem+json")
                .isNotNull();
        assertThat(contentType.toString())
                .as("server error Content-Type must be application/problem+json")
                .startsWith("application/problem+json");

        for (String forbidden : FORBIDDEN_INTERNAL) {
            assertThat(body)
                    .as("server error body must not leak internal detail '%s'", forbidden)
                    .doesNotContain(forbidden);
        }

        assertThat(body)
                .as("server error body should carry a generic, safe message")
                .containsIgnoringCase("unexpected error");
    }
}
