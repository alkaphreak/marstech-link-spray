---
status: accepted
date: 2026-11-21
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-276
---

# Hexagonal packages enforced by ArchUnit inside the single application

## Context and Problem Statement

The code is organised by technical layer (`controller`, `service`, `entity`, `repository`,
`dto`), so every feature is spread across the same packages. Services return Mongo entities,
throw `ResponseStatusException`, take `HttpServletRequest` and `MultipartFile` parameters, and
reach into other features' repositories (`CronJobService` reads `ApiKeyRepository` directly):
30 files under `service/` hold 112 Spring or `jakarta.servlet` imports. MLS-275 plans a move to
several services, which needs feature boundaries that do not exist today. How should the code be
structured so that each feature has a clear, enforced boundary, without splitting the
application yet?

## Considered Options

- Hexagonal packages in one Maven module, rules enforced by ArchUnit tests
- Hexagonal Maven modules (core module without Spring or Mongo dependencies)
- Package-by-feature without ports, no automated check

## Decision Outcome

Chosen option: "Hexagonal packages in one Maven module, rules enforced by ArchUnit tests",
because it draws the boundaries MLS-275 needs while keeping one build, one artifact and one
deployment, and a failing test catches a boundary breach in CI.

This ADR does not replace MLS-97's single-application decision: it prepares the split that
MLS-275 will decide. An extracted service takes its `domain/<feature>` package and its adapters.

**Domains** (`domain/<name>/`, each with its model, outbound ports and services)

| Domain        | Current classes                                                                  |
|---------------|----------------------------------------------------------------------------------|
| `shortener`   | Shortener, Spray, RandomIdGenerator, `LinkItem`, `BotUserAgents`                 |
| `paste`       | Paste                                                                            |
| `dashboard`   | Dashboard, `DashboardItem`, `DashboardLink`, `DashboardTheme`                    |
| `image`       | Image (storage behind `ImageStoragePort`)                                        |
| `nativetools` | Ocr, ImageCompression, `NativeWorkPermit`                                        |
| `document`    | MarkdownToPdf, PdfToMarkdown                                                     |
| `checklist`   | Checklist                                                                        |
| `cron`        | CronJob, `CronJobValidator`                                                      |
| `apikey`      | ApiKey (creation, regeneration, hashing behind `PasswordHasherPort`)             |
| `abuse`       | ReportAbuse                                                                      |
| `signature`   | WriteSignature                                                                   |
| `devtools`    | Dns, Ip, QrCode, RandomNumber, `SleepCycles`, uuid                               |

Spray stays with the shortener because it creates short links through the shortener service. The class lists name today's classes: in the first PR the Mongo documents among them (`LinkItem`,
`DashboardItem`, `DashboardLink`) stay in `adapter/outbound/mongo/<domain>`, and the second PR adds the
domain models they map to.

**`domain/shared/`** holds only interfaces, value types and exceptions, checked by ArchUnit:

- `Namespace` and `ApiKeyResolver` (implemented by the `apikey` domain), so other domains never
  see API key storage.
- `ClientContext` (IP or IPv6 /64, base URL, user agent), built by the web adapter, replacing
  `HttpServletRequest` in service signatures.
- `Page<T>` replacing Spring Data's `Page`, `PageRequest` and `Sort`.
- `QuotaPort` (implemented by the Bucket4j limiter of ADR 0006) and `MailPort`.
- Domain errors: `NotFound`, `Conflict`, `QuotaExceeded`, `PayloadTooLarge`, `Busy` (the native
  work permit), `InvalidInput`.

**Adapters** (`inbound`/`outbound` rather than `in`/`out`: `in` is a Kotlin keyword, so a package
named `in` would need backticks in every declaration and import)

- `adapter/inbound/web/<domain>/`: REST and Thymeleaf controllers, DTOs, DTO constraint annotations
  (`NotEmptyNotBlank`, `ValidUrl`, `ValidUrlList`), `ViewNameEnum`, `CommonConfs`, the single
  `X-Api-Key` constant, both exception handlers. A view controller calls a domain service, never
  another controller. Admin views render DTOs, not documents.
- `adapter/inbound/mcp/`: MCP tools.
- `adapter/outbound/mongo/<domain>/`: `@Document` classes with their current collection names and a
  `@TypeAlias`, Spring Data repositories, port implementations, index and TTL creation at
  startup, mapping between documents and domain types.
- `adapter/outbound/scheduler/`: `ThreadPoolTaskScheduler`, cron trigger, Spring `CronExpression`
  parsing (the 6-field behaviour of MLS-220), job re-registration at startup, behind
  `CronSchedulerPort`.
- `adapter/outbound/http/`: the cron webhook client.
- `adapter/outbound/{s3,dns,mail,geoip,native,ratelimit,security}/`: DigitalOcean Spaces, JNDI DNS,
  SMTP, MaxMind, tesseract/cwebp/caesiumclt subprocesses, Bucket4j, BCrypt.
- `adapter/outbound/web/`: `ReservedPaths` (reads `RequestMappingHandlerMapping`) behind
  `ReservedPathsPort`.
- `config/`: security, CSP, rate-limit and S3 configuration, `@ConfigurationProperties`.
- `utils/` is split: `ImageTypes` (magic-byte detection, used by `image`, `nativetools` and the
  image controller) becomes an `ImageType` enum with its pure `detect(bytes)` in
  `domain/shared`, upload validation still excluding SVG; `NetworkUtils` goes to `adapter/inbound/web`,
  `BotUserAgents` to `shortener` with its list loaded by an adapter (no `ClassPathResource` in
  the domain), `SleepCycles` to `devtools` with its result types (no `dto` import).
- Domain classes that still use Spring today get a destination for that dependency:
  `CronJobValidator` parses expressions through `CronSchedulerPort`, `NativeWorkPermit` becomes
  a `@Service` (not `@Component`), and domain models carry no `@ValidUrl`: URL validation stays
  on the DTOs (`DashboardLink`'s constraint moves to its request DTO).
  `objects/` and `enums/` are distributed to the domain or adapter that uses them. `exception/`
  becomes the `domain/shared` errors.

**Rules checked by ArchUnit** (`com.tngtech.archunit:archunit-junit5`, a new test dependency,
in a version that reads Java 25 bytecode)

- `domain..` depends on no Spring class except `org.springframework.stereotype.Service`, and
  not on `com.mongodb..`, `jakarta.servlet..` or `..adapter..`.
- `..adapter.inbound..` does not depend on `..adapter.outbound..`.
- A `domain.<name>` package does not depend on another `domain.<name>` package, except
  `domain.shared`.
- `domain.shared` contains only interfaces, data classes, enums and exceptions.

**Mongo**

- Collection names do not change.
- Stored `_class` values do not change: from the first PR, every moved `@Document` class and
  every class stored under a polymorphic field gets `@TypeAlias` set to its current fully
  qualified name. `DashboardEntity.items` is a `MutableList<DashboardItem>` (an interface), so
  each stored item holds `_class: "fr.marstech.mtlinkspray.entity.DashboardLink"`; without the
  alias, existing dashboards stop loading after the move.
- An alias resolves only for a type the mapping context already knows. `DashboardLink` is not a
  `@Document`, so Boot's entity scan does not register it and its alias would not resolve on a
  cold start. Concrete types stored under a polymorphic field are therefore registered at
  startup (explicit `MongoManagedTypes` / initial entity set) in the first PR, and an E2E case
  reads an old dashboard in a fresh context before any write.
- Domain models carry the `version` field. The checklist adapter keeps `@Version` and translates
  `OptimisticLockingFailureException` into a domain `Conflict`, which the handler maps to 409.
- Atomic updates (`updateFirst` with `arrayFilters` for checklist items, `findAndModify` for
  cron failures) stay in the adapters, behind intent-named port methods such as
  `updateItemStatus` and `incrementFailures`.

**Conventions**

- No inbound port interfaces: controllers and MCP tools call domain services directly.
- Single-implementation service interfaces (`XxxService` + `XxxServiceImpl`) are removed.
- Domain services throw domain errors and return domain types, never Mongo documents.

**Prerequisite: end-to-end regression suite** (MLS-277), merged and green before the first migration PR
is opened, and left unchanged by both migration PRs:

- `@SpringBootTest(classes = [MtLinkSprayApplication::class], webEnvironment = RANDOM_PORT)`
  with real HTTP calls and a real MongoDB started by flapdoodle through its Spring Boot 4
  integration `de.flapdoodle.embed.mongo.spring4x`. The `de.flapdoodle.embed.mongo` 5.0.0 pin
  (unused so far) is aligned with the version `spring4x` brings, or dropped.
- Tests live in a package outside `fr.marstech.mtlinkspray` and import no production class
  except `MtLinkSprayApplication`, which stays where it is, so the migration cannot move or
  rewrite them. An ArchUnit rule checks it.
- Documents seeded with the current `_class` values (a dashboard with items included) must
  still load, so a `_class` regression fails the suite.
- Every public route (REST, Thymeleaf pages, `/{code}` redirect, `/mcp`), each with at least one
  success and one error case, plus the behaviours the migration can break: 409 on a stale
  checklist version, 429 from the ADR 0006 quotas, `X-Api-Key` namespaces, TTL indexes, admin
  pages behind HTTP Basic.
- Runs in `mvn verify` and in the CI workflow.
- The `mt.link-spray.*` property names the suite sets (`e2e` profile, quota overrides) are part
  of the contract: the `@ConfigurationProperties` of the second PR keep them, otherwise the suite
  would silently fall back to defaults (real object storage, installed binaries).

**Migration** in two pull requests, each merged with `--squash` (the repository requires signed
commits and linear history, so a rebase merge, which GitHub does not sign, cannot be used):

1. Pure move: packages and imports only, behaviour and tests unchanged, with two exceptions
   that keep stored documents readable: the `@TypeAlias` annotations and the startup
   registration of polymorphic types. Thymeleaf `T(...)` references are updated with it. Its squash commit goes into `.git-blame-ignore-revs`.
2. Refactors: ports, domain types, domain errors, `ClientContext`, `@ConfigurationProperties`,
   removal of the interface + Impl pairs, of the dead `MtLinkSprayCollection*` code and of the
   duplicated `namespaceOf()`. The ArchUnit rules land in this PR and must pass.

Before the release of each PR, a smoke run against the dev MongoDB (port 27019) with
existing data checks that documents with the old `_class` values still load, that a stale
checklist version returns 409, and that the `cron_runs` and write-signature TTL indexes are
unchanged. `ViewRenderingTest` covers every public page, `/ocr` included. Both PRs are announced
to the parallel sessions working on the repository beforehand.

### Consequences

- Good: each feature's boundary is visible in the package tree and enforced in CI.
- Good: domain services can be unit-tested without Spring Data or MockMvc.
- Good: the MLS-275 split becomes moving packages, not untangling them.
- Bad: mapping code between Mongo documents and domain types for every domain, counted as new
  code by the 80% Sonar gate.
- Bad: the first PR touches almost every file and conflicts with any open branch.
- Bad: tolerating `@Service` keeps one Spring annotation in the domain.

## Pros and Cons of the Options

### Hexagonal Maven modules

- Good: the compiler, not a test, blocks a Spring or Mongo import in the core.
- Bad: a multi-module build, release and JaCoCo/Sonar setup for a single deployable, and
  `@Service` could not be used in the core.

### Package-by-feature without ports, no automated check

- Good: smallest change, no mapping code.
- Bad: services keep depending on Mongo and HTTP types, and nothing stops cross-feature
  imports from coming back, so it does not give MLS-275 its boundaries.
