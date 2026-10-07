package academy.rti.smc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Validated transaction payload.
 *
 * <p>Bean Validation (a control carried forward from a prior lesson) constrains
 * the inbound body. It is not the focus of this lesson — authentication and
 * authorization are.</p>
 */
public record TransactionRequest(

        @NotBlank(message = "account is required")
        @Size(max = 64, message = "account must be at most 64 characters")
        String account,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        BigDecimal amount
) {
}
