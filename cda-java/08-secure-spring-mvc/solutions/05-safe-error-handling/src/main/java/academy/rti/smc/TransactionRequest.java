package academy.rti.smc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Validated request DTO for creating a transaction.
 *
 * <p>Pre-baked prior control: input validation. {@code accountId} must not be blank
 * and {@code amount} must be positive. A blank {@code accountId} triggers a
 * {@code MethodArgumentNotValidException} on the controller.
 */
public record TransactionRequest(

        @NotBlank(message = "accountId must not be blank")
        String accountId,

        @Positive(message = "amount must be positive")
        BigDecimal amount
) {
}
