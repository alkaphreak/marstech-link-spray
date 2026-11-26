# AGENTS.md — marstech-link-spray

> URL shortener, link spray, paste service, and developer tools API.
> Maintainer: Alkaphreak (Stephane Robin). YouTrack project: **MLS**.
> For detailed Kotlin/Spring coding standards, see `.github/copilot-instructions.md`.

---

## Stack

- **Language**: Kotlin 2.4, Java 25 (Eclipse Temurin)
- **Framework**: Spring Boot 4.1, Spring WebMVC, Spring Security 7 (HTTP Basic, admin routes only)
- **Build**: Maven (`pom.xml`)
- **DB**: MongoDB (primary) — dev `localhost:27017`, tests an embedded MongoDB 8.0.5 started by flapdoodle `spring4x` for every full-context test (`de.flapdoodle.mongodb.embedded.version` in the `test` profile), prod `$MONGODB_URI_LINK_SPRAY`
- **Object storage**: Digital Ocean Spaces (S3-compatible, AWS SDK v2) — image storage (MLS-203)
- **Testing**: JUnit 5 + Mockito-Kotlin + MockMvc + jqwik (property-based) — profile `test`, with the embedded MongoDB above
  (the older full-context tests still `@MockitoBean` their repository-backed services). A `@WebMvcTest` that renders a view
  needs `RootApiController` in `controllers` (the layout reads its version); a new view goes into
  `ViewRenderingTest`'s `@CsvSource`
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
├── MtLinkSprayApplication.kt
├── domain/<name>/   # Services, per feature (ADR 0007): shortener (+ spray, random ids, bot UAs),
│                   #   paste, dashboard, image, nativetools (OCR, compression, NativeWorkPermit),
│                   #   document (md2pdf, pdf2md), checklist, cron (+ CronJobValidator), apikey,
│                   #   abuse, signature, devtools (DNS, IP, QR, random number, sleep cycles)
├── domain/shared/   # domain errors (PayloadTooLargeException, ForbiddenException, ConflictException,
│                   #   BusyException, QuotaExceededException), ImageType, MailPort, ApiKeyResolver +
│                   #   ApiCaller (other domains' only view of API keys), PasswordHasher,
│                   #   ClientContext (caller IP and user agent, built by NetworkUtils.clientContext)
├── adapter/inbound/
│   ├── web/<name>/  # REST + Thymeleaf controllers and their DTOs, per domain
│   ├── web/common/  # Exception handlers, ViewNameEnum, CommonConfs, NetworkUtils, URL validators,
│   │               #   root/privacy pages, RootApiController
│   └── mcp/         # MCP server tools on /mcp (Sol4McpTools, MLS-266, ADR 0004)
├── adapter/outbound/
│   ├── mongo/<name>/ # @Document classes (with @TypeAlias = pre-move class name), repositories and
│   │               #   port implementations (MongoApiKeyStore, MongoPasteStore, MongoAbuseReportStore, MongoShortLinkStore, MongoDashboardStore, MongoChecklistStore, MongoCronStore, MongoWriteSignatureStore
│   │               #   with its TTL index)
│   ├── security/    # BCryptPasswordHasher
│   ├── s3/ dns/ geoip/ botagents/ mail/ native/ ratelimit/ scheduler/ http/ web/ urlhaus/  # DigitalOcean Spaces, JNDI DNS,
│   │               #   DB-IP GeoIP, bot User-Agent lists, SMTP, Bucket4j AnonymousQuota, SpringCronScheduler, JdkWebhookClient (cron webhooks),
│   │               #   ReservedPaths (ReservedPathsPort), UrlhausReputation (UrlReputationPort, MLS-120)
└── config/          # Security, CSP, rate limit, S3, JSON body cap, DocumentConfig (md2pdf/pdf2md
                     #   services with their limits), NativeToolsConfig (OCR and image compression
                     #   services with their limits and quotas), ImageConfig (image upload limit),
                     #   ShortenerConfig (random id generator with mt.link-spray.random-id.*),
                     #   ChecklistConfig (checklist limits, mt.link-spray.checklist.*),
                     #   CronJobConfig (cron job limit and startup scheduling),
                     #   MongoConfig (managed types for
                    #   @TypeAlias resolution of polymorphic items, ADR 0007)
```

**Key entry points**:

- `MtLinkSprayApplication.kt` — Spring Boot main
- `ShortenerApiController` → `ShortenerService` → `ShortLinkStore` (`MongoShortLinkStore` over `LinkItemRepository`, core URL shortener flow)
- `SprayApiController` → `SprayService` — multi-URL spray
- `PasteApiController` → `PasteService` → `PasteStore` (`MongoPasteStore` → `PasteRepository`) — paste CRUD
- `DashboardApiController` → `DashboardService` → `DashboardStore` (`MongoDashboardStore`, which also creates the unique slug index) — collection management
- `ImageApiController` → `ImageService` → `ImageStore` (`MongoImageStore`) and `ImageStoragePort` (`DigitalOceanSpacesImageStorage`) — image storage (MLS-203)
- `AdminApiKeyViewController` → `ApiKeyService` → `ApiKeyStore` (`MongoApiKeyStore` → `ApiKeyRepository`) — admin page to create/list/enable-disable per-app API keys (`/admin/api-keys`, HTTP Basic protected)
- `Md2PdfApiController` → `MarkdownToPdfService` — raw Markdown body to PDF, `X-Api-Key` (MLS-223)
- `Pdf2MdApiController` → `PdfToMarkdownService` — raw PDF body to Markdown (PDFBox), `X-Api-Key` (MLS-196)
- `OcrApiController` → `OcrService` — raw PNG/JPEG body, or a scanned PDF of one page rendered at 300 dpi (MLS-253), to text through a `tesseract` subprocess behind the `OcrEngine` port (`adapter/outbound/native/TesseractOcrEngine`, apt package in both Dockerfiles), one OCR at a time (503 when busy), anonymous per-IP quota lifted by `X-Api-Key` (429), web page `/ocr` (MLS-252)
- `ImageCompressionApiController` → `ImageCompressionService` — `POST /api/img/compress`, raw PNG/JPEG body compressed or converted through `cwebp` (WebP) and `caesiumclt` (JPEG/PNG) subprocesses behind the `ImageEncoder` port (`adapter/outbound/native/NativeImageEncoder`, installed in both Dockerfiles, ADR 0002), sharing the `NativeWorkPermit` with OCR, pixel caps 8 MP WebP / 8 MP JPEG / 3 MP PNG, anonymous per-IP quota lifted by `X-Api-Key` (MLS-108)
- `CronJobApiController` → `CronJobService` → `CronStore` (`MongoCronStore`, which also creates the lookup and TTL indexes), `CronSchedulerPort` (`SpringCronScheduler`) and `WebhookClient` (`JdkWebhookClient`) — cron jobs per `X-Api-Key` namespace (MLS-220, ADR 0003): HTTP webhook on a Spring 6-field cron (min 1 min), scheduled in memory by `SpringCronScheduler` and re-registered at `ApplicationReadyEvent` (`CronJobConfig`), runs in `cron_runs` (TTL `mt.link-spray.cron.run-retention-days`), one alert mail when consecutive failures reach the threshold; `CronJobValidator` rejects non-public targets (SSRF) at creation, on an update that changes the URL, and again at each run, redirects are not followed. Single instance only: a second one runs every job twice
- `ChecklistApiController` → `ChecklistService` → `ChecklistStore` (`MongoChecklistStore`, which also creates the namespace indexes) — checklist templates and instances per `X-Api-Key` namespace on `/api/checklists` (MLS-222, ADR 0005): an instance copies its template's items at creation and never resyncs; template replace carries the `version` read (`@Version`, stale -> 409 via `ConflictException`); item status is one atomic `updateFirst` with `arrayFilters`; progress `DONE / (total - SKIPPED)` computed, not stored; limits under `mt.link-spray.checklist.*`
- `Sol4McpTools` → `DnsService`, `IpService`, `QrCodeService`, `RandomNumberService` — MCP server on `/mcp`, stateless Streamable HTTP, no session kept (Spring AI, MLS-266, ADR 0004): 6 read-only `@McpTool` methods (uuid, random_number, dns_lookup, dns_reverse, ip_lookup, qr_code as base64 PNG), inputs refused exactly as by the REST API, no authentication, no DNS propagation

**Critical flows**:

- Shorten URL: `GET /api/url-shortener/shorten?url=` (or `POST /api/url-shortener`, 201 Created, MLS-258) → `ShortenerApiController` → `ShortenerService` → `ShortLinkStore` (MongoDB)
- Resolve short code: `GET /{code}` → `UrlRedirectController` → `ShortenerService` → redirect; the User-Agent is matched in memory by `BotUserAgents` (vendored patterns in `resources/bot-user-agents/`, refresh steps in its README): robots bump `botClickCount`, people `clickCount` and `lastClickedAt` (MLS-273)
- Create paste: `POST /api/paste` → `PasteApiController` (`PasteRequest.toDraft()`, `ClientContext`) → `PasteService` → `PasteStore`
- Abuse report: `POST /abuse` (form) → `ViewAbuseController` (`ClientContext`) → `ReportAbuseService` → `AbuseReportStore` (`MongoAbuseReportStore`), then `SmtpMailSender`
- Rate limit (MLS-161, ADR 0006): `RateLimitConfig` registers a `HandlerInterceptor` on the public write routes, API and form (`/api/url-shortener`, `GET`/`HEAD /api/url-shortener/shorten`, `/api/paste`, `/api/dashboard[/*]`, `/shortener`, `/paste`, `/dashboard`: one shared `writeQuota` bean per client) and on `POST /abuse` (own budget, it sends a mail); a shortened spray pays one token per link in `SprayService`. `AnonymousQuota` is a Bucket4j token bucket per IPv4 or IPv6 /64 in a bounded Caffeine cache, 429 with `Retry-After` (JSON, or the error page for `Accept: text/html`); limits under `mt.link-spray.rate-limit.*`, a window of 0 disables them (test profile). A new public write route must be added to `RateLimitConfig`
- Upload image: `POST /api/images` (header `X-Api-Key: {keyId}.{secret}`) → `ImageApiController` → `ImageService` (key checked through `ApiKeyResolver`) → `DigitalOceanSpacesImageStorage` → DO Spaces bucket `mt-mls-img-storage`
- Download image: `GET /api/images/{id}` → `ImageApiController` streams bytes from DO Spaces (no redirect), enforces visibility/ownership
- Manage API keys: `GET/POST /admin/api-keys` (HTTP Basic) → `AdminApiKeyViewController` → `ApiKeyService` (create/list/enable-disable)
- Cron page: `GET /cron` → `ViewCronController` (no model); the page's JS calls `/api/cron/jobs` with the key typed in the page (sessionStorage), toggling a job is a `PUT` of the whole job with `isEnabled` flipped, API values rendered with `textContent` only (MLS-265)
- Fingerprint page: `GET /fingerprint` → `ViewFingerprintController` (User-Agent, Accept-Language from the request) + client-side signals (Canvas, WebGL, audio, Intl, navigator, screen) hashed with `crypto.subtle`; nothing is sent or stored (MLS-254 phase A)

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
- **Property-based tests** (jqwik, MARSTECH-847): for pure functions with invariants (parsers,
  validators, generators), in a `*PropertyTest` class next to the unit test. Always pin
  `@Property(seed = ...)` so runs are deterministic; defaults live in
  `src/test/resources/junit-platform.properties`
- **E2E regression suite** (MLS-277, ADR 0007 prerequisite): `src/test/kotlin/fr/marstech/e2e/`, black-box HTTP
  against a RANDOM_PORT app and the embedded MongoDB, profile `e2e` alone: `src/test/resources/application-e2e.properties`
  holds every value it relies on, not `application-test.properties` (FakeS3 in-memory object storage, stand-in
  scripts in `src/test/resources/e2e-bin/` for tesseract/cwebp/caesiumclt, test DB-IP files). It imports no production class except `MtLinkSprayApplication` (`ArchitectureE2eTest`), so a
  refactor cannot rewrite it: a change that breaks it is a behaviour change, not a test to adapt. API keys are
  created through the admin page (`E2eTest.Admin`). `MongoCompatibilityE2eTest` reads documents stored with
  today's `_class` values on a cold context; `QuotaE2eTest` runs with small buckets in its own context.
  A new public route gets a success and an error case there. Field injection in `E2eTest` (port,
  `MongoTemplate`) is the one exception to constructor injection, to keep subclasses free of constructors
- **Architecture rules** (ADR 0007, MLS-276): `ArchitectureTest` checks the hexagonal boundaries with no
  freeze: any violation fails the build. New code goes where the ADR says
- **Settings**: a group of `mt.link-spray.*` keys is a `@ConfigurationProperties` data class next to the
  config that uses it, with the defaults in the class; `@Value` is left for single keys in adapters and
  for the S3 keys (required only when `S3Config` is active) and the admin credentials (`@Value` fails
  startup on an unresolved `${...}`, the properties binder would keep it as a literal). The API key header name is
  `API_KEY_HEADER` (`adapter/inbound/web/common`)
- **DTOs for all API responses** — never return raw entities from controllers
- **Schema-first**: data classes with validation annotations are the contract, not comments
- **ADRs**: architecture decisions live in `docs/adr/` (MADR; create it from the Marstech-Configs
  `project-init` template with the first ADR). Read them before changing a design they cover
- **API versioning**: content negotiation (`Accept: application/json;version=N`, 1.0 by default,
  ADR 0001). A breaking change adds a `version = "2"` method next to the v1 one; URLs never change
- **Spring Security is scoped to `/admin/**` only** (HTTP Basic) — `SecurityConfig` `permitAll()`s everything else. CSRF is enforced on `/admin/**` only: admin forms must use `th:action` so Spring Security injects the token; the public forms (spray, paste, dashboard) carry no token and stay exempt. `SecurityConfig` also sends the Content-Security-Policy on every response (`CONTENT_SECURITY_POLICY`, enforced since MLS-269): a new external origin (CDN, API, image host) must be added there, or the browser blocks it. Any new `@WebMvcTest` must `@Import(SecurityConfig::class)`, otherwise Spring Boot falls back to its default security autoconfig (login form + CSRF) and breaks the test.
- **No inline script or style in templates** (MLS-269, Content-Security-Policy): a page script goes in
  `static/assets/js/pages/<view>.js`, CSS in `static/assets/css/sol4.css`, and a behavior that would be an
  `on*=` handler is a `data-*` attribute handled in `mt-script.js` (`data-copy-input`, `data-copy-key`,
  `data-reload`, `data-no-submit`, `data-confirm`). Build DOM with `textContent`, never `innerHTML`.
  `TemplateCspComplianceTest` fails the build otherwise
- **No Bootstrap, no CDN** (MLS-271): the UI is MarsTech-Skin (`static/assets/skin/`, synced from the
  MarsTech-Skin repo, never edited only here). The Bootstrap class names the markup still uses are
  re-implemented on the Skin tokens in `sol4.css`; a new such class needs a rule there
  (`BootstrapSubsetCoverageTest`). Icons are self-hosted in `static/assets/vendor/bootstrap-icons/`. The
  burger and alert close buttons run from `mt-script.js` (`data-menu-toggle`, `data-dismiss="alert"`).
  Every page wraps its content in one `<main id="main">`, the skip link's target (`TemplateCspComplianceTest`)
- **Request-param validation**: `ValidationConfig` sets `adaptConstraintViolations`, so a `@Validated`
  controller throws `MethodValidationException` (mapped to 400, MLS-244). To reproduce it in a test, use
  `@WebMvcTest` with `@Import(ValidationConfig::class)`: a standalone MockMvc never runs that validation
- **Typed request params**: a typed `@RequestParam` (`Int`, `LocalTime`, ...) that fails to convert is already a 400
  (`GlobalRestExceptionHandler.handleMethodArgumentTypeMismatch`): check that handler before claiming a 500 (MLS-213)
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

# Run dev: MongoDB from docker-compose.dev.yml (container marstech-mongodb-dev, port 27019), app on :8096
docker compose -f docker-compose.dev.yml up -d
rtk mvn spring-boot:run -f pom.xml -Dspring-boot.run.profiles=dev

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
- **Prod instance**: `apps-s-1vcpu-0.5gb` (512 MB, 1 shared vCPU): time any native or subprocess workload
  with `docker run --cpus=1 --memory=512m` on the prod image before setting its timeout (MLS-252: OpenMP
  threads sized on the host cores made OCR twice as slow). No local Docker daemon: run it on AlkaNaz
  (`sudo -n /usr/local/bin/docker`, Marstech-Configs `llm/instructions/runners.md`), then confirm on prod after
  the release. App Platform cuts a request at about 30 s with a 504 (MLS-253)
- **Changelog**: move `[Unreleased]` into `[X.Y.Z]` (changelogger skill) and merge it before dispatching
  `release.yml`, which never edits `CHANGELOG.md` (v0.6.6, v0.7.0 and v0.8.0 shipped without a section, MLS-254)
- **Release bump**: choose `release.yml`'s `bump` input by semver from what the release holds, never the
  `patch` default unchecked: any new feature or endpoint `minor` (0.y.z included), fixes only `patch`, a
  breaking API change `major` (rule in Marstech-Configs `llm/shared/context.md`; v0.3.0 added pdf2md)
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

_Last updated: 2026-11-23 — apikey, paste, signature, document and abuse domains on ports (MLS-276)_
