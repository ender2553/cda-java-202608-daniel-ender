# Paint Quest - Exercise

## Goal

Complete this graded Console exercise. The application is a paint calculator: users register/login, calculate gallons and cost for a room, receive color advice, and review saved estimates.

Start with the in-memory profile:

```bash
Run test
```

The application starts interactively and remains in the menu until you select `Q`/`quit`. The in-memory repositories are already supplied so you can run the console while working. The JDBC profile is intentionally incomplete.

Username: paintmaster
Password: ColorQuest2026!

## Required TODOs

1. `AuthService.register` — validate, BCrypt-hash, and save a user.
2. `AuthService.login` — verify with `PasswordEncoder.matches` and return a generic error.
3. `JdbcUserRepository.save` — implement insert/update with `JdbcTemplate`.
4. `JdbcUserRepository.findByUsername` — implement a parameterized query and row mapping.
5. `JdbcEstimateRepository.save` — insert an estimate with `JdbcTemplate`.
6. `JdbcEstimateRepository.findByUserId` — map rows back to `PaintEstimate` records.
7. `SecurityReviewExample` — repair the intentionally insecure password, SQL, collection, and money-handling examples. The tests identify when each repair is complete.

Run the in-memory application first. Then configure PostgreSQL and test the JDBC profile:

```bash
PAINT_PROFILE=jdbc mvn spring-boot:run
```

The database schema is in `src/main/resources/schema.sql`.

## Security requirements

- Never store plaintext passwords.
- Use BCrypt through the injected `PasswordEncoder`.
- Use one generic authentication error.
- Keep SQL values parameterized with `?` placeholders.
- Preserve constructor validation and `BigDecimal` for money.
- Do not expose mutable internal collections.

## Concept checklist

Identify these in the code before submitting: encapsulation, abstraction, inheritance, polymorphism, interfaces, records, sealed interfaces, enums, collections, factory pattern, repository pattern, Spring dependency injection, constructor validation, defensive copying, BCrypt, allow-lists, `BigDecimal`, and parameterized SQL.




