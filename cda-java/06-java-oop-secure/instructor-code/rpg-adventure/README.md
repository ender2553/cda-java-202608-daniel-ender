# RPG Adventure 

An educational Spring Boot 3 / Java 21 Maven CLI RPG demonstrating secure OOP and persistence.

The project includes inline JavaDoc and security callouts explaining why the code validates
inputs, uses BCrypt, avoids plaintext passwords, copies mutable state at boundaries, uses immutable
records, restricts polymorphism with sealed types, uses allow-lists, and binds SQL parameters.



For PostgreSQL, create `rpg_adventure`, a least-privilege user, and set `RPG_DB_URL`, `RPG_DB_USER`, and `RPG_DB_PASSWORD`. The schema is initialized by Spring. Never commit credentials.

## Concepts covered include the following and more...

- Encapsulation and copy-in/copy-out: `PlayerCharacter`, `SaveSnapshot`
- Inheritance and polymorphism: `Combatant`, `AbstractCombatant`, `PlayerCharacter`, `Monster`
- Interfaces and dependency inversion: `UserRepository`, `CharacterRepository`, `MonsterFactory`
- Sealed types: `Combatant`, `AdventureAction`
- Records: `Item`, `UserAccount`, `SaveSnapshot`, action results
- Factory pattern: `MonsterFactory` / `DefaultMonsterFactory`
- Repository pattern: memory, file, and JDBC implementations selected with Spring profiles
- Security: BCrypt work factor 12, password allowlist/length validation, generic login errors, no password exposure, parameterized SQL, immutable snapshots, final fields

The console starts automatically and continues until the player enters `quit`, `exit`, or `end`.
The first login can register a new account.
