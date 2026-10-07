package academy.rti.smc;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO bound from the JSON body of POST /api/transactions.
 *
 * <p>SOLUTION STATE: each field carries Jakarta Bean Validation constraints.
 * When the controller binds the body with {@code @Valid}, a violated constraint
 * raises {@code MethodArgumentNotValidException}, which Spring Boot maps to
 * HTTP 400 Bad Request automatically.
 */
public class TransactionRequest {

    @NotBlank
    private String accountId;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;

    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;

    @Size(max = 280)
    private String memo;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
