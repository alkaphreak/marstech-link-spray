# AGENTS.md — marstech-link-spray

> URL shortener, link spray, paste service, and developer tools API.
> Maintainer: Alkaphreak (Stephane Robin). YouTrack project: **MLS**.
> For detailed Kotlin/Spring coding standards, see `.github/copilot-instructions.md`.

---

## Stack

- **Language**: Kotlin 2.x, Java 21 (Eclipse Temurin)
- **Framework**: Spring Boot 3.5+, Spring WebMVC
- **Build**: Maven (`pom.xml`)
- **DB**: MongoDB (primary) — dev `localhost:27017`, test `localhost:27018`, prod `$MONGODB_URI_LINK_SPRAY`
- **Testing**: JUnit 5 + Mockito + MockMvc — profile `test`
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

| Type                     | Pattern                                    | Example                          |
|--------------------------|--------------------------------------------|----------------------------------|
| Specification            | `{ISSUE-ID}-specification.md`              | `MLS-129-specification.md`       |
| Implementation summary   | `{ISSUE-ID}-{PHASE}-COMPLETE.md`           | `MLS-129-PHASE2-COMPLETE.md`     |
| Progress tracking        | `{ISSUE-ID}-progress.md`                   | `MLS-139-progress.md`            |
| Session notes            | `{ISSUE-ID}-session{N}-{topic}.md`         | `MLS-129-session1-auth.md`       |

See `.local/llm/README.md` for complete conventions.

---

## Key Rules for Agents

- **Prefer Kotlin over Java** for all new code
- **Constructor injection** — never `@Autowired` on fields
- **Test method names**: camelCase only — `shouldReturnUrlWhenCodeExists()`. No backticks.
- **Test structure**: Given-When-Then
- **DTOs for all API responses** — never return raw entities from controllers
- **Schema-first**: data classes with validation annotations are the contract, not comments
- **Conventional commits** for all commit messages

---

## Terminal Commands

```bash
# Build + test
rtk mvn clean verify -f /Users/marstechadmin/IdeaProjects/marstech-link-spray/pom.xml

# Run dev (requires MongoDB on 27017)
rtk mvn spring-boot:run -f /Users/marstechadmin/IdeaProjects/marstech-link-spray/pom.xml

# Run tests only
rtk mvn test -f /Users/marstechadmin/IdeaProjects/marstech-link-spray/pom.xml
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

```bash
MyGraphify /Users/marstechadmin/IdeaProjects/marstech-link-spray
/graphify query "find all callers of ShortenerService"
/graphify explain "URL resolution flow"
```

---

_Last updated: 2026-06-04 — MARSTECH-643 AGENTS.md standard rollout_
