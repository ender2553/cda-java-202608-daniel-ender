package academy.rti.smc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * IDENTICAL test in both the student (RED) and instructor (GREEN) trees.
 *
 * The transport-layer assertions (custom security header + least-privilege CORS
 * allow-list) FAIL against the permissive student SecurityConfig and PASS against
 * the instructor SecurityConfig.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecureTransportTest {

    private static final String ALLOWED_ORIGIN = "https://app.example.com";
    private static final String EVIL_ORIGIN = "https://evil.example.com";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/health carries the required Referrer-Policy security header")
    void healthCarriesRequiredSecurityHeader() throws Exception {
        // Spring Security's default adds X-Content-Type-Options: nosniff, so we also
        // sanity-check it, but the *graded* requirement is an explicitly-configured
        // Referrer-Policy header, which the default filter chain does NOT add.
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    @Test
    @DisplayName("CORS preflight from the allowed origin is granted (origin echoed)")
    void corsPreflightFromAllowedOriginIsGranted() throws Exception {
        mockMvc.perform(options("/api/transactions")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
    }

    @Test
    @DisplayName("CORS preflight from a disallowed origin is NOT granted")
    void corsPreflightFromEvilOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/transactions")
                        .header(HttpHeaders.ORIGIN, EVIL_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    @DisplayName("Stateless JSON POST succeeds WITHOUT a CSRF token (csrf deliberately disabled)")
    void postTransactionSucceedsWithoutCsrfToken() throws Exception {
        String body = """
                {
                  "accountId": "acct-123",
                  "amount": 49.99,
                  "currency": "USD"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
