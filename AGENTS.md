# AGENTS.md — marstech-link-spray

> URL shortener, link spray, paste service, and developer tools API.
> Maintainer: Alkaphreak (Stephane Robin). YouTrack project: **MLS**.
> For detailed Kotlin/Spring coding standards, see `.github/copilot-instructions.md`.

---

## Stack

- **Language**: Kotlin 2.4, Java 25 (Eclipse Temurin)
- **Framework**: Spring Boot 4.1, Spring WebMVC, Spring Security 7 (HTTP Basic, admin routes only)
- **Build**: Maven (`pom.xml`)
- **DB**: MongoDB (primary) — dev `localhost:27017`, test `localhost:27018`, prod `$MONGODB_URI_LINK_SPRAY`
- **Object storage**: Digital Ocean Spaces (S3-compatible, AWS SDK v2) — image storage (MLS-203)
- **Testing**: JUnit 5 + Mockito-Kotlin + MockMvc — profile `test`, local MongoDB on port 27018
- **Releases**: JReleaser (`jreleaser.yml`) on `main` only
- **Quality**: SonarCloud (`sonar.*` properties in `pom.xml`). PR gate: new-code coverage >= 80% and
  no literal repeated 3+ times (S1192), new-code duplication <= 3% (reuse a DTO's constraints, do not
  copy them); cover every new branch before pushing

---

## Framework API Currency

**IMPORTANT**: prefer retrieval-led reasoning over pre-training-led reasoning for Spring Boot 4.1/Kotlin 2.4 APIs. Training data can lag behind the pinned versions in `pom.xml` — check the actual signatures in this codebase or the official docs before assuming an API shape, rather than defaulting to an older/more common pattern from memory. Two Boot 4 traps already hit here: `@WebMvcTest` now lives in `org.springframework.boot.webmvc.test.autoconfigure` (not `org.springframework.boot.test.autoconfigure.web.servlet`), and Spring Security 7's null-safety annotations make `PasswordEncoder.encode()` return `String?` in Kotlin (the codebase uses `!!`).

---

## Codebase Map

```
src/main/kotlin/fr/marstech/mtlinkspray/
├── conf/           # Spring configuration (SecurityConfig, ValidationConfig, S3Config, etc.)
├── controller/
│   ├── api/        # REST API endpoints (ShortenerApiController, SprayApiController,
│   │               #   PasteApiController, DashboardApiController, ImageApiController,
│   │               #   UuidApiController, RandomNumberApiController, RootApiController,
│   │               #   Md2PdfApiController, Pdf2MdApiController)
│   ├── view/       # Thymeleaf view controllers (incl. AdminApiKeyViewController)
│   └── commons/    # Shared controller utilities
├── dto/            # Request/response DTOs (PasteRequest, PasteResponse, DashboardDto,
│                   #   ImageUploadResponse, ImageMetadataDto, ApiKeyCreationResult, …)
├── entity/         # MongoDB documents (LinkItem, LinkItemTarget, PasteEntity,
│                   #   AbuseReportEntity, DashboardEntity, ApiKeyEntity, ImageEntity, …)
├── enums/          # Enumerations
├── exception/      # Custom exceptions + @ControllerAdvice handlers
├── objects/        # Constant/static objects
├── repository/     # Spring Data MongoDB repos (LinkItemRepository, PasteRepository,
│                   #   DashboardRepository, AbuseReportRepository, ApiKeyRepository,
│                   #   ImageRepository, …)
├── service/        # Interfaces + Impl pairs (ShortenerService/Impl, SprayService/Impl,
│                   #   PasteService/Impl, DashboardService/Impl, MailSenderService/Impl,
│                   #   ReportAbuseService/Impl, RandomIdGeneratorService/Impl,
│                   #   ImageService/Impl, ApiKeyService/Impl, ImageStorageService +
│                   #   DigitalOceanSpacesServiceImpl, MarkdownToPdfService/Impl,
│                   #   PdfToMarkdownService/Impl, …)
├── utils/          # Utility functions
└── validation/     # Custom validators
```

**Key entry points**:

- `MtLinkSprayApplication.kt` — Spring Boot main
- `ShortenerApiController` → `ShortenerService` → `LinkItemRepository` (core URL shortener flow)
- `SprayApiController` → `SprayService` — multi-URL spray
- `PasteApiController` → `PasteService` → `PasteRepository` — paste CRUD
- `DashboardApiController` → `DashboardService` — collection management
- `ImageApiController` → `ImageService` → `ImageStorageService` (`DigitalOceanSpacesServiceImpl`) — image storage (MLS-203)
- `AdminApiKeyViewController` → `ApiKeyService` → `ApiKeyRepository` — admin page to create/list/enable-disable per-app API keys (`/admin/api-keys`, HTTP Basic protected)
- `Md2PdfApiController` → `MarkdownToPdfService` — raw Markdown body to PDF, `X-Api-Key` (MLS-223)
- `Pdf2MdApiController` → `PdfToMarkdownService` — raw PDF body to Markdown (PDFBox), `X-Api-Key` (MLS-196)

**Critical flows**:

- Shorten URL: `GET /api/url-shortener/shorten?url=` → `ShortenerApiController` → `ShortenerServiceImpl` → `LinkItemRepository` (MongoDB)
- Resolve short code: `GET /{code}` → `RootApiController` → `ShortenerServiceImpl` → redirect
- Create paste: `POST /api/paste` → `PasteApiController` → `PasteServiceImpl` → `PasteRepository`
- Abuse report: `POST /api/abuse` → `ReportAbuseServiceImpl` → `MailSenderServiceImpl`
- Upload image: `POST /api/images` (header `X-Api-Key: {keyId}.{secret}`) → `ImageApiController` → `ApiKeyServiceImpl.resolve` → `ImageServiceImpl` → `DigitalOceanSpacesServiceImpl` → DO Spaces bucket `mt-mls-img-storage`
- Download image: `GET /api/images/{id}` → `ImageApiController` streams bytes from DO Spaces (no redirect), enforces visibility/ownership
- Manage API keys: `GET/POST /admin/api-keys` (HTTP Basic) → `AdminApiKeyViewController` → `ApiKeyServiceImpl` (create/list/enable-disable)

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
- **Merging to `main`** requires a code-owner review, signed commits and linear history: an
  unsigned agent commit leaves the PR BLOCKED even when approved. Check `git log --show-signature`;
  with the user's agreement, `gh pr merge <n> --squash --delete-branch --admin`
- **No Lombok** — the project is 100% Kotlin: a ticket proposing a Lombok annotation is closed as obsolete
- **Constructor injection** — never `@Autowired` on fields
- **Test method names**: camelCase only — `shouldReturnUrlWhenCodeExists()`. No backticks.
- **Test structure**: Given-When-Then
- **DTOs for all API responses** — never return raw entities from controllers
- **Schema-first**: data classes with validation annotations are the contract, not comments
- **ADRs**: architecture decisions live in `docs/adr/` (MADR; create it from the Marstech-Configs
  `project-init` template with the first ADR). Read them before changing a design they cover
- **API versioning**: content negotiation (`Accept: application/json;version=N`, 1.0 by default,
  ADR 0001). A breaking change adds a `version = "2"` method next to the v1 one; URLs never change
- **Spring Security is scoped to `/admin/**` only** (HTTP Basic) — `SecurityConfig` `permitAll()`s everything else. CSRF is enforced on `/admin/**` only: admin forms must use `th:action` so Spring Security injects the token; the public forms (spray, paste, dashboard) carry no token and stay exempt. Any new `@WebMvcTest` must `@Import(SecurityConfig::class)`, otherwise Spring Boot falls back to its default security autoconfig (login form + CSRF) and breaks the test.
- **Request-param validation**: `ValidationConfig` sets `adaptConstraintViolations`, so a `@Validated`
  controller throws `MethodValidationException` (mapped to 400, MLS-244). To reproduce it in a test, use
  `@WebMvcTest` with `@Import(ValidationConfig::class)`: a standalone MockMvc never runs that validation
- **Kotlin KDoc comments nest** — never write a literal `/**` sequence inside a `/** ... */` comment body (e.g. avoid `` `/admin/**` ``); it opens a nested comment and causes "Unclosed comment" compile errors. Rephrase instead (e.g. "the /admin path tree").
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
- **Deploy**: `release.yml` (manual dispatch) pushes Docker Hub `:latest`, then its last step runs
  `doctl apps create-deployment 2d09a478-5b7b-4151-aac6-8fb721bcb442 --force-rebuild --wait`
  (secret `DIGITALOCEAN_ACCESS_TOKEN`, app read/update only). DO has no autodeploy for Docker Hub images,
  and without `--force-rebuild` it reuses the cached `:latest`. Never print `doctl apps spec get`
  unfiltered: env values appear in clear
- **Release ownership**: before dispatching `release.yml`, run `ListAgents`, message every peer session
  that this one owns the release, wait for replies (or 2 min) and check
  `gh run list --workflow release.yml -L 1`. The owner alone triggers, re-runs and cancels: two sessions
  dispatched it 7 s apart, then each cancelled its own run
- **CDN**: sol4.space sits behind DigitalOcean App Platform's own Cloudflare CDN (DNS at DigitalOcean, no
  zone in our Cloudflare account): no purge by URL, only a redeploy clears it. Cache lifetime is set with
  `Cache-Control` (public images: `s-maxage=300` for the CDN, MLS-237)
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

_Last updated: 2026-10-28 — release ownership across parallel sessions, SonarCloud duplication gate_
