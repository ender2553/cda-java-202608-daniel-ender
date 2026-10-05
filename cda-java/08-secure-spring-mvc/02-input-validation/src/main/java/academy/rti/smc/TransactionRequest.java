package academy.rti.smc;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO bound from the JSON body of POST /api/transactions.
 *
 * <p>STARTER STATE: this DTO carries NO validation constraints. Any input the
 * client sends is accepted as-is, so malformed transactions flow straight into
 * the controller and return 201 Created.
 *
 * <p>TODO: add Bean Validation.
 *   - accountId : must not be blank        -> @NotBlank
 *   - amount    : required, positive,
 *                 at most 2 decimal places -> @NotNull @Positive @Digits(integer=..,fraction=2)
 *   - currency  : exactly 3 uppercase
 *                 letters (ISO-4217-ish)   -> @Pattern(regexp = "^[A-Z]{3}$")
 *   - memo      : at most 280 characters   -> @Size(max = 280)
 *
 * See the matching solution in the instructor project for the full annotation set.
 */
public class TransactionRequest {

    // TODO: add Bean Validation (@NotBlank)
    @NotBlank
    private String accountId;

    // TODO: add Bean Validation (@NotNull @Positive @Digits(integer = 12, fraction = 2))
    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;

    // TODO: add Bean Validation (@Pattern(regexp = "^[A-Z]{3}$"))
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;

    // TODO: add Bean Validation (@Size(max = 280))
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
