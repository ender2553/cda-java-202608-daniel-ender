package academy.rti.smc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Identical in both the student (RED) and instructor (GREEN) trees.
 *
 * <p>Asserts the authentication + role-based authorization contract:
 * anonymous writes are rejected (401), authenticated-but-under-privileged
 * writes are rejected (403), the correctly-privileged write succeeds (201),
 * and the public health probe stays open (200).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class TransactionAuthTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String WELL_FORMED = "{\"account\":\"acct-001\",\"amount\":42.50}";

    @Test
    void anonymousPostTransaction_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(WELL_FORMED))
                .andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void viewerPostTransaction_isForbidden() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(httpBasic("viewer", "viewer-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(WELL_FORMED))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    void transactorPostWellFormedTransaction_isCreated() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .with(httpBasic("transactor", "transactor-pass"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(WELL_FORMED))
                .andExpect(status().isCreated()); // 201
    }

    @Test
    void anonymousGetHealth_isOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk()); // 200 — public path stays open
    }
}
