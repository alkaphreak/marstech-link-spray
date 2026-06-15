---
apply: always
---

# Generic AI Assistant Rules — marstech-link-spray

These rules apply to all AI assistants working in this repository, regardless of tool or model.

---

## Language

- **Kotlin is the only accepted language** for `src/main/kotlin/`. Do not generate Java files.
- Use idiomatic Kotlin: `val`, `data class`, `when`, extension functions, `?.let`, safe calls.
- No `!!` (non-null assertion) without an explanatory comment justifying it.

---

## Naming Conventions

| Element             | Convention                            | Example                               |
|---------------------|---------------------------------------|---------------------------------------|
| Classes             | PascalCase                            | `ShortenerServiceImpl`                |
| Functions/variables | camelCase                             | `findByShortCode()`                   |
| Constants           | SCREAMING_SNAKE_CASE                  | `MAX_RETRY_COUNT`                     |
| Test methods        | camelCase, descriptive, no backticks  | `shouldReturn404WhenCodeNotFound()`   |
| Packages            | lowercase dot-separated               | `fr.marstech.mtlinkspray.service`     |
| Environment vars    | SCREAMING_SNAKE_CASE                  | `MONGODB_URI_LINK_SPRAY`              |

---

## Code Structure

- **Constructor injection only** — never `@Autowired` on fields or `lateinit var` for injected dependencies.
- **DTOs for all API I/O** — never return `@Document` entity classes from controllers.
- **Schema-first** — `data class` with validation annotations is the API contract.
  Do not use loose `Map<String, Any>` as return or parameter types in controllers.
- **Sealed classes** for modelling state/result variants.
- **`val` over `var`** everywhere possible.

---

## Testing

- **Given-When-Then** structure in every test method.
- Test method names: **camelCase only**, no backticks, descriptive intent.
  - Correct: `shouldThrowExceptionWhenUrlIsInvalid()`
  - Wrong: `` `should throw exception when url is invalid`() ``
- Use `@DisplayName` for human-readable descriptions (optional but encouraged).
- Use **Mockito-Kotlin** (`org.mockito.kotlin`) for mocking — not raw Java Mockito.
- Tests requiring MongoDB use profile `test` (port 27018, `docker-compose.test.yml`).

---

## API Design

- Follow REST conventions: correct HTTP verbs and status codes (200, 201, 400, 404, 409, 500).
- Validate all inputs with JSR-380 annotations (`@NotBlank`, `@Valid`, etc.).
- Return consistent `ErrorResponse` DTOs from `@ControllerAdvice` for all errors.
- No hardcoded strings in controllers — use constants or configuration.

---

## Output Style

- **No emojis** in generated code, comments, or commit messages.
- **Professional tone** in all code comments and documentation.
- **Conventional commits** format for all commit messages:
  `type(scope): short description`
  When a YouTrack ticket is available, append the ticket ID and full URL on a new line at the end of the commit message:
  ```
  feat(shortener): add expiry support

  MLS-142 https://marstech.myjetbrains.com/youtrack/issue/MLS-142
  ```
- KDoc comments on all public API functions and classes.

---

## What NOT to Generate

- Java files in the Kotlin source tree.
- Field injection (`@Autowired lateinit var`).
- Raw entity classes in controller responses.
- Test methods with backtick names.
- `!!` without justification.
- Hardcoded credentials, URLs, or port numbers (use `application.properties` / env vars).
- Files in `target/`, `.local/llm/`, or `**/secrets/**`.

---

## Living Documentation

When adding or changing a feature, also update:

- `AGENTS.md` — codebase map, critical flows, stack versions
- `CLAUDE.md` — if build/run commands or conventions change
- `.github/copilot-instructions.md` — if Copilot-specific guidance changes

---

_Last updated: 2026-06-05 — MLS-139 AI-agent readiness_