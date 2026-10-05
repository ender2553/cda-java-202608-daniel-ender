package academy.rti.smc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Identical in both the student (starter) and instructor (solution) trees.
 *
 * <p>RED/GREEN contract:
 * <ul>
 *   <li>STUDENT (no validation): the well-formed test passes; all six bad-input
 *       tests FAIL because the controller returns 201 instead of 400.</li>
 *   <li>INSTRUCTOR (validation present): all seven tests pass.</li>
 * </ul>
 */
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String body(String accountId, String amount, String currency, String memo) {
        return """
                {
                  "accountId": %s,
                  "amount": %s,
                  "currency": %s,
                  "memo": %s
                }
                """.formatted(
                json(accountId),
                amount == null ? "null" : amount,   // amount is a raw JSON number/literal
                json(currency),
                json(memo));
    }

    private String json(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @Test
    void wellFormedRequestReturns201() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACCT-1001", "125.50", "USD", "Coffee with client")))
                .andExpect(status().isCreated());
    }

    @Test
    void blankAccountIdReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", "125.50", "USD", "valid memo")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeAmountReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACCT-1001", "-10.00", "USD", "valid memo")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void amountWithTooManyDecimalsReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACCT-1001", "125.555", "USD", "valid memo")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidCurrencyReturns400() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACCT-1001", "125.50", "usd", "valid memo")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedMemoReturns400() throws Exception {
        String oversizedMemo = "x".repeat(281);
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ACCT-1001", "125.50", "USD", oversizedMemo)))
                .andExpect(status().isBadRequest());
    }
}
