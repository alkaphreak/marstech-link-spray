# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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