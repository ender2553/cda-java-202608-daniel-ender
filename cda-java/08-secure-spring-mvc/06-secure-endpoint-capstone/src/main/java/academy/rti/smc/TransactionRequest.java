package academy.rti.smc;

// STARTER DTO — UNVALIDATED.
//
// TODO (Bean Validation): add constraints to every field so a malformed body
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
        String accountId,
        java.math.BigDecimal amount,
        String currency,
        String memo
) {
}
