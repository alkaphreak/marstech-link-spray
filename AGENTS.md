# AGENTS.md — marstech-link-spray

> URL shortener, link spray, paste service, and developer tools API.
> Maintainer: Alkaphreak (Stephane Robin). YouTrack project: **MLS**.
> For detailed Kotlin/Spring coding standards, see `.github/copilot-instructions.md`.

---

## Stack

- **Language**: Kotlin 2.3, Java 21 (Eclipse Temurin)
- **Framework**: Spring Boot 3.5+, Spring WebMVC
- **Build**: Maven (`pom.xml`)
- **DB**: MongoDB (primary) — dev `localhost:27017`, test `localhost:27018`, prod `$MONGODB_URI_LINK_SPRAY`
- **Testing**: JUnit 5 + Mockito-Kotlin + MockMvc — profile `test`, local MongoDB on port 27018
- **Releases**: JReleaser (`jreleaser.yml`) on `main` only
- **Quality**: SonarCloud (`sonar-project.properties`)

---

## Codebase Map

```
src/main/kotlin/fr/marstech/mtlinkspray/
├── conf/           # Spring configuration (SecurityConfig, ValidationConfig, etc.)
├── controller/
│   ├── api/        # REST API endpoints (ShortenerApiController, SprayApiController,
│   │               #   PasteApiController, DashboardApiController,
│   │               #   UuidApiController, RandomNumberApiController, RootApiController)
│   ├── view/       # Thymeleaf view controllers
│   └── commons/    # Shared controller utilities
├── dto/            # Request/response DTOs (PasteRequest, PasteResponse, DashboardDto, …)
├── entity/         # MongoDB documents (LinkItem, LinkItemTarget, PasteEntity,
│                   #   AbuseReportEntity, DashboardEntity, …)
├── enums/          # Enumerations
├── exception/      # Custom exceptions + @ControllerAdvice handlers
├── objects/        # Constant/static objects
├── repository/     # Spring Data MongoDB repos (LinkItemRepository, PasteRepository,
│                   #   DashboardRepository, AbuseReportRepository, …)
├── service/        # Interfaces + Impl pairs (ShortenerService/Impl, SprayService/Impl,
│                   #   PasteService/Impl, DashboardService/Impl, MailSenderService/Impl,
│                   #   ReportAbuseService/Impl, RandomIdGeneratorService/Impl, …)
├── utils/          # Utility functions
└── validation/     # Custom validators
```

**Key entry points**:

- `MtLinkSprayApplication.kt` — Spring Boot main
- `ShortenerApiController` → `ShortenerService` → `LinkItemRepository` (core URL shortener flow)
- `SprayApiController` → `SprayService` — multi-URL spray
- `PasteApiController` → `PasteService` → `PasteRepository` — paste CRUD
- `DashboardApiController` → `DashboardService` — collection management

**Critical flows**:

- Shorten URL: `POST /api/shorten` → `ShortenerApiController` → `ShortenerServiceImpl` → `LinkItemRepository` (MongoDB)
- Resolve short code: `GET /{code}` → `RootApiController` → `ShortenerServiceImpl` → redirect
- Create paste: `POST /api/paste` → `PasteApiController` → `PasteServiceImpl` → `PasteRepository`
- Abuse report: `POST /api/abuse` → `ReportAbuseServiceImpl` → `MailSenderServiceImpl`

---

## AI Exclusions

Never auto-scan or modify:

- `target/` — Maven build output
- `.local/llm/` — LLM-generated docs (read when explicitly requested)
- `**/secrets/**`, `application-prod.properties` — credentials

---

## LLM Documentation Convention

All AI-generated specs, analyses, and session notes go in **`.local/llm/`** — never in the project root.

| Type                   | Pattern                            | Example                      |
|------------------------|------------------------------------|------------------------------|
| Specification          | `{ISSUE-ID}-specification.md`      | `MLS-129-specification.md`   |
| Implementation summary | `{ISSUE-ID}-{PHASE}-COMPLETE.md`   | `MLS-129-PHASE2-COMPLETE.md` |
| Progress tracking      | `{ISSUE-ID}-progress.md`           | `MLS-139-progress.md`        |
| Session notes          | `{ISSUE-ID}-session{N}-{topic}.md` | `MLS-129-session1-auth.md`   |

See `.local/llm/README.md` for complete conventions.

---

## Key Rules for Agents

- **Prefer Kotlin to Java** for all new code
- **Constructor injection** — never `@Autowired` on fields
- **Test method names**: camelCase only — `shouldReturnUrlWhenCodeExists()`. No backticks.
- **Test structure**: Given-When-Then
- **DTOs for all API responses** — never return raw entities from controllers
- **Schema-first**: data classes with validation annotations are the contract, not comments
- **Conventional commits** for all commit messages — append ticket ID and full YouTrack URL on a trailing line when available:
  ```
  feat(shortener): add expiry support

  MLS-142 https://marstech.myjetbrains.com/youtrack/issue/MLS-142
  ```

---

## Terminal Commands

From the project root and using rtk :

```bash
# Build + test
rtk mvn clean verify -f pom.xml

# Run dev (requires MongoDB on 27017)
rtk mvn spring-boot:run -f pom.xml

# Run tests only
rtk mvn test -f pom.xml
```

---

## External Integrations

- **YouTrack**: project key `MLS` — `https://marstech.myjetbrains.com/youtrack`
- **SonarCloud**: `alkaphreak_marstech-link-spray`
- **Docker Hub**: `alkaphreak/marstech-link-spray`
- **URL Shortener (self)**: `GET https://sol4.space/api/url-shortener/shorten?url={url}`

---

## Living Documentation

When adding or modifying a feature, update this file:

- Add new controllers/services to the Codebase Map
- Update Critical flows if a new flow is introduced
- Record any new naming conventions or project-specific patterns

---

## Graphify Knowledge Graph

If `graphify-out/` exists at the repo root:

~~~bash
MyGraphify .
/graphify query "find all callers of ShortenerService"
/graphify explain "URL resolution flow"
~~~

---

## Maintaining This File

This file is **living documentation**. Any agent (or human) making changes to the project MUST keep it up to date in the same commit or PR.

| Change type                              | What to update                                                     |
|------------------------------------------|--------------------------------------------------------------------|
| New controller or service                | Add to `## Codebase Map` and `## Critical flows` if flow changes   |
| New external integration                 | Add to `## External Integrations`                                  |
| New naming convention or coding pattern  | Add to `## Key Rules for Agents`                                   |
| New build/run command                    | Add to `## Terminal Commands`                                      |
| Stack version bump (Kotlin, Spring, etc) | Update `## Stack` with new version                                 |
| New AI-exclusion path                    | Add to `## AI Exclusions`                                          |

**Rule**: Never leave this file describing a state that no longer reflects the repository.

---

_Last updated: 2026-06-05 — MLS-139 AI-agent readiness_
