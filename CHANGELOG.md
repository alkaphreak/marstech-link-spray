# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

---

## [0.10.0] - 2026-11-09

### Added

- Sleep cycle calculator: page `/sleep` (shareable `?wakeAt=` / `?sleepAt=` links) and `GET /api/sleep`, 90-minute cycles plus 15 minutes to fall asleep (MLS-213)

---

## [0.9.2] - 2026-11-09

### Added

- Click counting on short links: each redirect increments an anonymous per-link counter (atomic, no IP,
  User-Agent or fingerprint), read through `GET /api/url-shortener/{uid}/stats`; the privacy page says so (MLS-261)

### Dependencies

- AWS SDK S3 2.57.3 → 2.57.10, commonmark 0.30.1 → 0.32.0, and the rest of the Dependabot maven group (#212)

---

## [0.9.1] - 2026-11-09

### Fixed

- Write signatures: the TTL index is retried in the background with a capped backoff, no longer on every web write while it is missing (MLS-254)
- Dashboards: the unique slug index is retried with a capped backoff when its creation fails at startup (MLS-260)

---

## [0.9.0] - 2026-11-08

### Added

- Public link pages at `/p/{slug}` on top of dashboards: vanity slug, bio and https avatar (MLS-115)

### Changed

- Editing a dashboard requires the `X-Api-Key` that created it; dashboards created without a key are
  read-only (MLS-115)

### Fixed

- Dashboard link items deserialize from JSON and expose their url (MLS-115)
- Creating a dashboard over an existing id returns 409 instead of overwriting it, and the check is atomic
  (insert) (MLS-115, MLS-259)
- Two dashboards can no longer claim the same slug at once: the unique slug index is now created at startup
  (MLS-259)
- The `LIGHT` theme, which left the page dark, is removed: public pages are dark only (MLS-259)

---

## [0.8.1] - 2026-11-06

### Fixed

- Web write forms (shortener, spray, paste) wait at most 2 s for the browser fingerprint, then submit without
  it, instead of hanging when a signal never settles (MLS-254)
- The 12-month TTL index on write signatures is retried on each write until created, so a MongoDB down at
  startup no longer leaves signatures stored without expiry (MLS-254)
- "Shorten and spray" from the web UI records a write signature for each per-URL short link, not only for the
  final spray link (MLS-254)

---

## [0.8.0] - 2026-11-04

### Added

- `POST /api/url-shortener` with a JSON body `{"url": ...}`: creates a short link and returns 201 Created, the
  short URL in `Location` and as `{"shortUrl": ...}`; `GET /api/url-shortener/shorten` is unchanged (MLS-258)
- `POST /api/img/compress?format=webp|jpeg|png&quality=&maxDimension=`: compresses or converts a raw PNG or
  JPEG body with `cwebp` or `caesiumclt` (ADR 0002), metadata stripped; capped at 8 MP for WebP and JPEG output,
  3 MP for PNG; `X-Image-Compressed: false` when the encoder failed and the original came back (MLS-108)

### Changed

- `POST /api/paste`, `POST /api/images` and `POST /api/dashboard` return 201 Created instead of 200 OK; the
  body is unchanged (MLS-258)
- OCR and image compression share one native-workload permit: a request arriving while the other runs gets a
  503 (MLS-108)

---

## [0.7.0] - 2026-11-04

### Added

- Write signatures: short links, shortened sprays and pastes created from the website record a browser fingerprint hash,
  the IP address and the User-Agent, deleted automatically after 12 months (MongoDB TTL index); admin lookup at
  `/admin/signatures`, privacy notice at `/privacy` (MLS-254)

---

## [0.6.6] - 2026-11-04

### Fixed

- Fingerprint page: the result is announced to screen readers, one failing signal no longer blocks the others, and the copy button reports a clipboard failure (MLS-254)

---

## [0.6.5] - 2026-11-04

### Changed

- Visual redesign on the MarsTech-Skin design system ("Glacier neon"): the Bootswatch Pulse theme is
  replaced by the Skin tokens and components with self-hosted fonts, Bootstrap components follow the
  tokens, and pages now set a viewport so phones no longer render at desktop width (MLS-250)
- Tools menu: one row from 992px, a burger opening a vertical list below, instead of a single
  overflowing row (MLS-250)

### Added

- Leading icons on the Generate (random), Generate URL (spray) and Get shortened link buttons (MLS-250)

---

## [0.6.4] - 2026-11-03

### Added

- API keys admin: regenerate a key's secret while keeping its key id and namespace (MLS-209)

### Fixed

- API keys admin: toggling and regenerating a key update a single field each, so one can no longer
  revert the other (MLS-209)

---

## [0.6.3] - 2026-11-02

### Fixed

- OCR: PNG and JPEG images above a pixel budget are rejected before reaching tesseract (MLS-256)

---

## [0.6.2] - 2026-11-02

### Added

- Property-based tests with jqwik (#182)

### Fixed

- OCR: the PDF raster is bounded by a per-axis pixel budget, and PDF loading now runs inside the concurrency permit and the request deadline, so a large PDF can no longer exhaust memory or outlive its timeout (MLS-255)

---

## [0.6.1] - 2026-10-31

### Added

- Fingerprint page: button to copy the fingerprint report (MLS-254)

---

## [0.6.0] - 2026-10-31

### Added

- Browser fingerprint transparency page (MLS-254)

### Fixed

- OCR: scanned PDFs limited to one page (MLS-253)

---

## [0.5.0] - 2026-10-31

### Added

- OCR of scanned PDFs of up to 2 pages (MLS-253)

---

## [0.4.1] - 2026-10-30

### Fixed

- OCR: tesseract runs with a single OpenMP thread (MLS-252)

---

## [0.4.0] - 2026-10-30

### Added

- OCR: `POST /api/ocr` and `/ocr` page (MLS-252)

---

## [0.3.0] - 2026-10-29

### Added

- pdf2md: `POST /api/pdf2md`, PDF body converted to Markdown (MLS-196)

### Fixed

- Shortener web form validates URL length and protocol (MLS-59)
- `Allow` header kept on 405 responses, stale QR code hidden while shortening (MLS-251)

---

## [0.2.14] - 2026-10-29

### Added

- IP tools: caller IP, country, ASN and WebRTC leak check (MLS-113)

---

## [0.2.13] - 2026-10-28

### Added

- Markdown to PDF endpoint (MLS-223)
- DNS lookup, reverse lookup and propagation tools (MLS-104)
- mailto link generator page (MLS-175)
- Every Thymeleaf view is rendered through the full context in tests (MLS-245)

### Fixed

- Bulk QR: blank lines counted in the error, shortening error kept visible (MLS-247)
- Shortener inserts instead of saving, and retries on a duplicate ID (MLS-246)

---

## [0.2.12] - 2026-10-27

### Added

- Bulk QR code generation: `POST /api/qr/bulk`, one QR code per line (up to 100), downloaded as a ZIP, and a Bulk mode on `/qr` (MLS-241)
- REST API versioned by content negotiation (`Accept: application/json;version=1`, 1.0 by default, so current clients keep working); an unsupported version returns 400 (MLS-137, ADR 0001)

### Fixed

- Release: skipping an already published package matches the maven-deploy-plugin 3.x 409 message

---

## [0.2.11] - 2026-10-27

### Added

- Paste page links to a QR code of the paste URL

---

## [0.2.10] - 2026-10-27

### Changed

- Request handling and task executors run on virtual threads (MLS-224)

### Fixed

- Request parameter validation errors return 400 with field errors (MLS-244)

---

## [0.2.9] - 2026-10-27

### Added

- "Shorten first" toggle on `/qr`

### Changed

- Public images use a 5-minute CDN lifetime instead of a Cloudflare purge on delete
- Maven Dependabot updates grouped into one PR

### Fixed

- Shortener never generates a short ID equal to a route
- Shortener ID cache refill is bounded and cache access serialized
- Upload limit read from configuration (MLS-240, MLS-239)

### Dependencies

- Spring Boot 4.1.1 → 4.1.2, jackson-module-kotlin 3.2.3 → 3.2.4, AWS SDK S3 2.56.0 → 2.57.3

---

## [0.2.8] - 2026-10-23

### Changed

- JVM heap lowered to 45% of the container RAM

---

## [0.2.7] - 2026-10-23

### Added

- QR code generator page and API (MLS-110)

---

## [0.2.6] - 2026-10-23

### Changed

- JVM heap set to 70% of the container RAM

---

## [0.2.5] - 2026-10-23

### Added

- Copy button for a newly created API key (MLS-235)

### Fixed

- Image uploads return 413 when oversized and 400 when malformed

---

## [0.2.4] - 2026-10-23

### Fixed

- Private images are no longer cacheable by shared caches

---

## [0.2.3] - 2026-10-21

### Changed

- Release redeploys the DigitalOcean App Platform app and forces a re-pull of `:latest` (MLS-233)

---

## [0.2.2] - 2026-10-21

### Fixed

- MongoDB configured through `spring.mongodb.uri` for Spring Boot 4 (MLS-234)

---

## [0.2.1] - 2026-10-20

### Added

- Image storage on DigitalOcean Spaces (MLS-203)
- `shortenAndSpray` in `SprayService`
- GitHub link in the footer, Docker Hub badges in the README

### Changed

- CI: redundant workflows and duplicate caches removed (MLS-90)
- Simplified coroutine scope

### Fixed

- Unknown short links and paths return 404 (MLS-10)
- Docker: application runs on the Java 25 runtime (MLS-231), target jar kept in the build context (MLS-232)

### Dependencies

- Spring Boot 3.5.10 → 4.1.1, Kotlin 2.3.21 → 2.4.21, AWS SDK S3 2.46.21 → 2.56.0
- jackson-module-kotlin 2.21.3 → 2.22.0, then `tools.jackson.module` 3.0.3 → 3.2.3
- kotlinx-coroutines-core 1.10.2 → 1.11.0, commons-lang3 3.20.0 → 3.21.0, commons-collections4 4.5.0 → 4.6.0
- Test and build: mockito-kotlin 5.2.1 → 6.4.0, embedded MongoDB 4.24.0 → 5.0.0, JaCoCo 0.8.14 → 0.8.15, JReleaser 1.24.0 → 1.26.0
- GitHub Actions: checkout 7, cache 6, setup-java 6

---

## [0.2.0] - 2026-05-22

### Added

- `GET /paste/{id}/raw` — returns paste content as `text/plain` for CLI, programmatic, and AI agent access (MLS-186)
- `GET /api/paste/{id}/raw` — mirrors raw output in the API namespace (MLS-186)
- "Raw" button on the paste view page linking to `/paste/{id}/raw` (MLS-186)
- Optional `?password=` query param supported on both raw endpoints for password-protected pastes (MLS-186)
- `PasteViewControllerTest` with raw-endpoint test coverage (MLS-186)
- 4 raw-endpoint test methods added to `ApiPasteControllerTest` (MLS-186)
- `.sdkmanrc` to manage SDK versions consistently across environments (MLS-158)

### Changed

- Upgraded all major dependencies: Kotlin 2.3.10 → 2.3.21, Spring Boot, Jackson Kotlin Module, JReleaser Maven Plugin (MLS-158)
- Updated release workflow to use a Personal Access Token (`RELEASE_GH_TOKEN`) for PR merge step to bypass branch protection

### Fixed

- Improved GitHub Packages deployment error handling — gracefully skips on 409 Conflict instead of failing the workflow

### Dependencies

- `com.fasterxml.jackson.module:jackson-module-kotlin` bumped
- `kotlin.version` 2.3.10 → 2.3.21
- `org.jreleaser:jreleaser-maven-plugin` bumped
- `docker/setup-qemu-action` 3 → 4

---

## [0.1.2] - 2026-04-26

### Fixed

- Corrected version tagging issue after the v0.1.1 release; re-released to ensure consistent artifact and tag alignment

---

## [0.1.1] - 2026-04-25

### Fixed

- Release workflow: removed duplicate Maven settings, upgraded QEMU action, switched to PR-based merge strategy for release branch to main

---

## [0.1.0] - 2026-04-24

### Added

- Form validation on abuse, paste, random, and shortener pages with disabled submit buttons and user feedback (MLS-130)
- Generic JS form field validation with dynamic interaction handling (MLS-130)
- `RandomNumberServiceImpl` unit tests — 14 tests covering happy path, validation, and edge cases (MLS-130)
- Enhanced `RandomIdGeneratorServiceImplTest` with cache, uniqueness, and length checks (MLS-130)
- Controller test for null shortener service response returning HTTP 500 (MLS-130)
- Multi-platform Docker image support (`linux/amd64`, `linux/arm64`) via Buildx in CI (#63)
- Validation annotations and improved error handling for `PasteRequest` fields (#40)
- Additional SonarCloud quality badges to README (MLS-191)
- Dependabot configuration for Maven and GitHub Actions dependency automation (#47)

### Changed

- Extracted and decoupled `RandomNumberService` as a functional interface (MLS-130)
- URL shortener nullability handling in controller — returns HTTP 500 on null response (MLS-130)
- Guarded `db.sh` PATH with Docker CLI export for reliable script execution (MLS-130)

### Dependencies

- `org.apache.commons:commons-lang3` 3.18.0 → 3.20.0
- `commons-validator:commons-validator` 1.7 → 1.10.1
- `kotlin.version` 2.3.0 → 2.3.10
- `org.apache.commons:commons-collections4` bumped
- `com.fasterxml.jackson.module:jackson-module-kotlin` bumped
- `de.flapdoodle.embed:de.flapdoodle.embed.mongo` bumped
- `docker/setup-buildx-action` 3 → 4
- `actions/cache` 3 → 5
- `docker/login-action` 3 → 4
- `actions/upload-artifact` 4 → 7
- `actions/checkout` 4 → 6
- `org.jacoco:jacoco-maven-plugin` bumped
- `org.jreleaser:jreleaser-maven-plugin` bumped

---

## [0.0.5] - 2025-11-25

### Added

- JReleaser configuration for fully automated releases with GitHub Actions (MLS-135)
- Dockerfile template (`src/jreleaser/templates/docker`) for Docker Hub publishing (MLS-135)
- Distribution configuration for `mt-link-spray` as a `SINGLE_JAR` artifact
- JReleaser run script (`dev-ops/jreleaser-run.sh`) and test script (`dev-ops/test-jreleaser.sh`)
- Project metadata (homepage, VCS, bug tracker, icons, screenshots) in `jreleaser.yml`

### Changed

- Release workflow upgraded to use Temurin JDK distribution and Docker Buildx
- Docker image now sets active Spring profile to `prod` at build time
- JReleaser Maven plugin upgraded to 1.21.0

---

## [0.0.3] - 2025-04-24

### Added

- Initial implementation of the MarsTech Link Spray project.
- Added support for Spring Boot 3.4.3.
- Integrated Thymeleaf for template rendering.
- Added MongoDB support using Spring Data MongoDB.
- Configured Testcontainers for MongoDB integration tests.
- Added Kotlin support with version 2.1.0.
- Integrated MapStruct for object mapping.
- Added Apache Commons libraries for utility functions.
- Configured Maven build with Kotlin and MapStruct plugins.

### Changed

- Updated Maven project version to `0.0.3`.

### Deprecated

- Deprecated `@MockBean` usage in tests in favor of Mockito's `@Mock` and `@InjectMocks`.

### Fixed

- Resolved issues with Docker Desktop startup scripts on macOS.
- Fixed Thymeleaf template integration for displaying the application version.

### Removed

- Removed unused dependencies and configurations.

---

## [0.0.2] - YYYY-MM-DD

### Added

- Added initial project structure with Spring Boot and Maven.

## [0.0.1] - YYYY-MM-DD

### Added

- Project initialization.