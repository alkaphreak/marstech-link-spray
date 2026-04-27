# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

## [0.0.2] - YYYY-MM-DD

### Added

- Added initial project structure with Spring Boot and Maven.

## [0.0.1] - YYYY-MM-DD

### Added

- Project initialization.