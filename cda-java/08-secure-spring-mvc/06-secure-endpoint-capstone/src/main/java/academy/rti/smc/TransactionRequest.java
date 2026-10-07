package academy.rti.smc;

import jakarta.validation.constraints.*;

// STARTER DTO — UNVALIDATED.
//
// COMPLETE (Bean Validation): add constraints to every field so a malformed body
//      is rejected with HTTP 400 *before* the controller body runs:
//        accountId -> @NotBlank
//        amount    -> @NotNull @Positive @Digits(integer = 12, fraction = 2)
//        currency  -> @Pattern(regexp = "^[A-Z]{3}$")
//        memo      -> @Size(max = 280)
//      You will need: import jakarta.validation.constraints.*;
//
// As written, the record carries NO constraints, so adversarial test #4
// (invalid body -> 400 problem+json) FAILS: the body is accepted as-is.
public record TransactionRequest(
        @NotBlank
        String accountId,

        @NotNull @Positive @Digits(integer = 12, fraction = 2)
        java.math.BigDecimal amount,

        @NotNull @Pattern(regexp = "^[A-Z]{3}$")
        String currency,

        @Size(max = 280)
        String memo
) {
}
