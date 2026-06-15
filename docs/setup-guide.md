# Environment Setup Guide

This guide covers the complete local environment setup for MarsTech Link Spray development.

## Table of Contents

- [Prerequisites](#prerequisites)
- [SDK Version Management with SDKMan](#sdk-version-management-with-sdkman)
- [MongoDB Setup](#mongodb-setup)
- [Environment Variables](#environment-variables)
- [Build and Run](#build-and-run)
- [Verification](#verification)

---

## Prerequisites

| Tool     | Version  | Notes                    |
|----------|----------|--------------------------|
| Java     | 21 (LTS) | Temurin distribution     |
| Maven    | 3.9.x    | Build tool               |
| Kotlin   | 2.3.x    | Managed via Maven plugin |
| MongoDB  | 6.x+     | Or Docker                |
| SDKMan   | latest   | Recommended for SDK mgmt |
| Docker   | latest   | Optional, for MongoDB    |

---

## SDK Version Management with SDKMan

The project includes a `.sdkmanrc` file that pins exact SDK versions for reproducible builds.

### Install SDKMan

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

### Enable Auto-Env Switching

Edit `~/.sdkman/etc/config` and set:

```properties
sdkman_auto_env=true
```

With this setting, SDKMan automatically reads `.sdkmanrc` when you `cd` into the project directory.

### Install Project SDKs

```bash
cd /path/to/marstech-link-spray

# Install all versions declared in .sdkmanrc
sdk env install

# Apply versions for the current session (if auto-env is disabled)
sdk env
```

### Verify SDK Versions

```bash
java -version       # Should show Java 21 (Temurin)
mvn -version        # Should show Maven 3.9.x
kotlin -version     # Should show Kotlin 2.3.x
```

### Current .sdkmanrc

```properties
# .sdkmanrc - SDKMan version pins for this project
java=21.0.11-tem
maven=3.9.15
kotlin=2.3.21
springboot=3.5.14
```

> The `.sdkmanrc` file is tracked in version control to ensure consistency across all developer environments.

---

## MongoDB Setup

### Option A: Docker (Recommended for Development)

```bash
# Start MongoDB with Docker Compose
docker-compose -f docker-compose.dev.yml up -d

# Or use the helper script
./dev-ops/db.sh start
```

### Option B: Local MongoDB

Install MongoDB 6.x+ and ensure it is running on `localhost:27017`.

---

## Environment Variables

Set the following environment variables in your shell profile (`~/.zshrc` or `~/.bashrc`):

```bash
# Required for production profile
export MONGODB_URI_LINK_SPRAY="mongodb://localhost:27017/link-spray"

# Optional: SMTP configuration for mail notifications
export LINK_SPRAY_SMTP_HOST="mailtrap.io"
export LINK_SPRAY_SMTP_PORT="2525"
export LINK_SPRAY_SMTP_USER="your-user"
export LINK_SPRAY_SMTP_PASSWORD="your-password"
```

Reload your shell:

```bash
source ~/.zshrc
```

On macOS, also register with launchctl to avoid IDE restart:

```bash
launchctl setenv MONGODB_URI_LINK_SPRAY "$MONGODB_URI_LINK_SPRAY"
```

---

## Build and Run

```bash
# Build the project
mvn clean install

# Run with dev profile (MongoDB on localhost:27017)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Run with test profile (MongoDB on localhost:27018)
mvn test -Dspring.profiles.active=test
```

The application is accessible at `http://localhost:8096` by default.

---

## Verification

```bash
# Check application health
curl http://localhost:8096/actuator/health

# Check API version
curl http://localhost:8096/api/version
```

---

## References

- [SDKMan documentation](https://sdkman.io/usage)
- [Java 21 LTS](https://adoptium.net/temurin/releases/)
- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MongoDB documentation](https://www.mongodb.com/docs/)
