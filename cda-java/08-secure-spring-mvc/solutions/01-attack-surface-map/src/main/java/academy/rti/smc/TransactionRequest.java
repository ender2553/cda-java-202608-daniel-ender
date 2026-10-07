package academy.rti.smc;

import java.math.BigDecimal;

/**
 * DTO bound from the untrusted JSON request body of POST /api/transactions.
 *
 * NOTE (attack-surface lab): this is the baseline. There is intentionally
 * NO validation here. Every field is an entry point for untrusted input.
 */
public class TransactionRequest {

    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String memo;

    public TransactionRequest() {
    }

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
