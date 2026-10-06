package academy.rti.smc;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Validated request DTO.
 *
 * NOTE FOR THIS LAB: Bean Validation is PRE-BAKED (carried forward from the prior
 * control). It is intentionally NOT the focus of this lesson. The focus is the
 * transport-layer SecurityFilterChain (security headers + least-privilege CORS +
 * deliberate CSRF stance) in SecurityConfig.
 */
public record TransactionRequest(
        @NotBlank(message = "accountId is required")
        String accountId,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code")
        String currency
) {
}
