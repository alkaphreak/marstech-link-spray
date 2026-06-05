# CLAUDE.md — marstech-link-spray

> Claude Code context file. For full architecture and coding conventions, see `AGENTS.md` and
> `.github/copilot-instructions.md`.

---

## Stack

- **Language**: Kotlin 2.3, Java 21 (Eclipse Temurin)
- **Framework**: Spring Boot 3.5+, Spring WebMVC
- **Build**: Maven (`pom.xml`)
- **DB**: MongoDB — dev `localhost:27017`, test `localhost:27018`
- **Testing**: JUnit 5 + Mockito-Kotlin + MockMvc (local MongoDB on port 27018, profile `test`)

---

## Build & Run Commands

```bash
# Build + test (requires MongoDB running on 27018 for tests)
rtk mvn clean verify -f pom.xml

# Run tests only
rtk mvn test -f pom.xml

# Run dev server (requires MongoDB on 27017)
rtk mvn spring-boot:run -f pom.xml

# Package without tests
rtk mvn package -DskipTests -f pom.xml
```

> `rtk` is a local wrapper that sets the correct SDK versions (Java 21, Kotlin 2.3, Maven 3.x).
> If `rtk` is unavailable, use `mvn` directly with the project's `.mvn/` wrapper.

---

## Project Layout

```
src/main/kotlin/fr/marstech/mtlinkspray/
├── conf/           # Spring configuration classes
├── controller/
│   ├── api/        # REST controllers (@RestController)
│   ├── view/       # Thymeleaf view controllers
│   └── commons/    # Shared controller utilities
├── dto/            # Request/response DTOs (never return entities from controllers)
├── entity/         # MongoDB @Document classes
├── enums/          # Enumerations
├── exception/      # Custom exceptions + @ControllerAdvice
├── objects/        # Constants / singleton objects
├── repository/     # Spring Data MongoDB repositories
├── service/        # Interfaces + Impl pairs
├── utils/          # Pure utility functions
└── validation/     # Custom JSR-380 validators
```

---

## Critical Coding Rules

- **Kotlin only** — no Java files in `src/main/kotlin/`
- **Constructor injection** — never `@Autowired` on fields
- **DTOs for all API I/O** — never expose `entity/` classes from controllers
- **camelCase test method names** — no backtick-quoted names (e.g. `shouldReturnOkWhenUrlExists()`)
- **Given-When-Then** structure inside every test
- **`val` over `var`** — prefer immutability
- **`data class`** for DTOs and entities
- **Conventional commits** — when a YouTrack ticket is available, append ticket ID and full URL on a trailing line:
  ```
  feat(shortener): add expiry support

  MLS-142 https://marstech.myjetbrains.com/youtrack/issue/MLS-142
  ```
- **No `!!`** without an explicit justification comment
- **Schema-first** — data classes with validation annotations are the API contract

---

## Test Profile

Spring profile `test` activates `application-test.properties`:

```properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27018
```

Ensure a local MongoDB instance is running on port 27018 before running tests.
Use `docker-compose.test.yml` to spin one up:

```bash
docker compose -f docker-compose.test.yml up -d
```

---

## Files to Avoid Modifying

| Path                          | Reason                                       |
|-------------------------------|----------------------------------------------|
| `target/`                     | Maven build output — never edit directly     |
| `.local/llm/`                 | LLM-generated docs — read-only unless asked  |
| `application-prod.properties` | Production credentials                       |
| `**/secrets/**`               | Sensitive configuration                      |
| `jreleaser.yml`               | Release automation — change with caution     |

---

## Error Response Convention

All API errors return an `ErrorResponse` DTO:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Short code 'abc123' not found",
  "path": "/api/shorten/abc123"
}
```

Implemented via `@ControllerAdvice` in `exception/`.

---

## YouTrack Issue Keys

- Project key: `MLS`
- URL: `https://marstech.myjetbrains.com/youtrack`
- Reference issues in commit messages: `fix(paste): handle null content MLS-142`

---

## LLM Documentation

All AI-generated specs, analyses, and session notes go in `.local/llm/` — **never in the project root**.

| Type                   | Pattern                            |
|------------------------|------------------------------------|
| Specification          | `{ISSUE-ID}-specification.md`      |
| Implementation summary | `{ISSUE-ID}-{PHASE}-COMPLETE.md`   |
| Progress tracking      | `{ISSUE-ID}-progress.md`           |
| Session notes          | `{ISSUE-ID}-session{N}-{topic}.md` |

---

_Last updated: 2026-06-05 — MLS-139 AI-agent readiness_
