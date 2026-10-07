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
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Adversarial proof suite for the Secure-Endpoint Capstone.
 *
 * This identical suite runs against both the STUDENT starter and the
 * INSTRUCTOR solution. Against the starter most assertions are RED; against the
 * solution all are GREEN. Each test maps to one course control.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecureEndpointAdversarialTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_BODY = """
            {"accountId":"ACC-1001","amount":"125.50","currency":"USD","memo":"rent"}
            """;

    // 1) Authenticated transactor + well-formed body -> 201 Created.
    @Test
    @DisplayName("1: transactor + valid body -> 201")
    void transactorValidBodyCreated() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(httpBasic("transactor", "transactor-pw"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());
    }

    // 2) Anonymous POST -> 401 (authentication required).
    @Test
    @DisplayName("2: anonymous POST -> 401")
    void anonymousPostUnauthorized() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    // 3) Authenticated viewer (wrong role) POST -> 403 (authorization denied).
    @Test
    @DisplayName("3: viewer POST -> 403")
    void viewerPostForbidden() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(httpBasic("viewer", "viewer-pw"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    // 4) Transactor + invalid body -> 400 problem+json, no leaked internals.
    @Test
    @DisplayName("4: invalid body -> 400 problem+json, no stack trace / class name")
    void invalidBodyBadRequestProblemJson() throws Exception {
        String badBody = """
                {"accountId":"","amount":"-5","currency":"usd","memo":"%s"}
                """.formatted("x".repeat(300));

        MvcResult result = mockMvc.perform(post("/api/transactions")
                        .with(httpBasic("transactor", "transactor-pw"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("MethodArgumentNotValidException");
        assertThat(body).doesNotContain("Exception");
        assertThat(body).doesNotContain("at academy.rti.smc");
        assertThat(body).doesNotContain("\tat ");
    }

    // 5) Transactor + GET /api/transactions/boom -> 500 problem+json, generic
    //    message, NO internal detail leaked.
    @Test
    @DisplayName("5: boom -> 500 problem+json, generic, no internal detail")
    void internalErrorGenericProblemJson() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/transactions/boom")
                        .with(httpBasic("transactor", "transactor-pw")))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andReturn();

        String body = result.getResponse().getContentAsString().toLowerCase();
        assertThat(body).doesNotContain("jdbc");
        assertThat(body).doesNotContain("secret");
        assertThat(body).doesNotContain("datasource");
        assertThat(body).doesNotContain("exception");
        assertThat(body).doesNotContain("\tat ");
    }

    // 6) GET /api/health anonymous -> 200 (public).
    @Test
    @DisplayName("6: health anonymous -> 200")
    void healthAnonymousOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    // 7) Secure-transport header assertion: X-Content-Type-Options nosniff
    //    plus a deliberately-added Referrer-Policy: no-referrer. nosniff is a
    //    Spring Security default, but Referrer-Policy is NOT — the starter omits
    //    the headers() block entirely, so this is RED until the solution adds it.
    @Test
    @DisplayName("7: security headers present (nosniff + Referrer-Policy)")
    void securityHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
    }

    // 8) CORS least-privilege: preflight from the trusted origin is granted;
    //    preflight from an untrusted origin is NOT granted.
    @Test
    @DisplayName("8: CORS preflight trusted granted, untrusted denied")
    void corsLeastPrivilege() throws Exception {
        // Trusted origin -> Access-Control-Allow-Origin echoes the origin.
        mockMvc.perform(options("/api/transactions")
                        .header(HttpHeaders.ORIGIN, "https://app.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin",
                        equalTo("https://app.example.com")));

        // Untrusted origin -> not granted: header absent or not echoing evil.
        mockMvc.perform(options("/api/transactions")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().string("Access-Control-Allow-Origin",
                        not(equalTo("https://evil.example.com"))));
    }
}
