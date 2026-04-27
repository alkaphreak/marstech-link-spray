# Marstech - Link Spray

**Marstech - Link Spray** is a web application that allows you to open multiple URLs with a single click. Follow a few
simple steps to generate a unique link that will open all the specified URLs.

## Badges

[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-dark.svg)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![SonarQube](https://github.com/alkaphreak/marstech-link-spray/actions/workflows/maven-sonar.yml/badge.svg)](https://github.com/alkaphreak/marstech-link-spray/actions/workflows/maven-sonar.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=bugs)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=coverage)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)

## Features

- **Link Spray**: Enter multiple URLs and generate a single link to open them all at once.
- **URL Shortener**: Shorten long URLs and resolve short codes back to their original targets.
- **Paste Service**: Create, retrieve, and password-protect text pastes with syntax highlighting support.
- **Dashboard**: Manage collections of links and pastes via a personal dashboard.
- **Abuse Reporting**: Report abusive or malicious links directly from the interface.
- **Random Number Generator**: Generate random numbers within a configurable range via API.
- **UUID Generator**: Generate random UUIDs via API.
- **Random ID Cache**: Pre-generated ID cache for high-throughput short link creation.
- **Mail Notifications**: Email notification support via configurable SMTP settings.
- **Actuator**: Health check and endpoint mapping exposed via Spring Actuator.

## Technologies used

| Technology                  | Version        |
|-----------------------------|----------------|
| Kotlin                      | 2.3.0          |
| Java                        | 21 (Temurin)   |
| Spring Boot                 | 3.5.10         |
| Spring WebMVC               | (via Boot)     |
| Spring Data MongoDB         | (via Boot)     |
| Spring Security Crypto      | (via Boot)     |
| Spring Boot Actuator        | (via Boot)     |
| Thymeleaf                   | (via Boot)     |
| Bootstrap                   | (frontend)     |
| Maven                       | Build tool     |
| MongoDB                     | Database       |
| Docker                      | Dev/test       |
| JUnit 5 / Mockito           | Testing        |
| Flapdoodle Embed Mongo      | 4.21.0 (tests) |
| Apache Commons Lang3        | 3.20.0         |
| Apache Commons Collections4 | 4.4            |
| Jackson Kotlin Module       | 2.20.0         |
| Kotlinx Coroutines          | 1.10.2         |
| JReleaser                   | Releases       |

## Project Structure

```
src/main/kotlin/fr/marstech/mtlinkspray/
├── conf/           # Configuration classes and event handlers
├── controller/
│   ├── api/        # REST API endpoints
│   │   ├── DashboardApiController.kt
│   │   ├── PasteApiController.kt
│   │   ├── RandomNumberApiController.kt
│   │   ├── RootApiController.kt
│   │   ├── ShortenerApiController.kt
│   │   ├── SprayApiController.kt
│   │   └── UuidApiController.kt
│   └── view/       # Thymeleaf view controllers
│       ├── DashboardViewController.kt
│       ├── PasteViewController.kt
│       ├── UrlRedirectController.kt
│       ├── ViewAbuseController.kt
│       ├── ViewRootController.kt
│       ├── ViewShortenerController.kt
│       ├── ViewSprayController.kt
│       └── ViewUuidController.kt
├── dto/            # Data Transfer Objects
├── entity/         # MongoDB document entities
├── enums/          # Enumerations
├── exception/      # Custom exceptions
├── objects/        # Shared objects and constants
├── repository/     # Spring Data MongoDB repositories
├── service/        # Business logic interfaces and implementations
├── utils/          # Utility classes
└── validation/     # Custom validation annotations and validators
```

## API Endpoints

| Method | Path                         | Description                         |
|--------|------------------------------|-------------------------------------|
| GET    | `/api/version`               | Returns current application version |
| GET    | `/api/spray`                 | Generate a Link Spray URL           |
| GET    | `/api/url-shortener/shorten` | Shorten a URL                       |
| GET    | `/api/paste/{pasteId}`       | Retrieve a paste                    |
| POST   | `/api/paste`                 | Create a new paste                  |
| GET    | `/api/dashboard/{id}`        | Get a dashboard by ID               |
| POST   | `/api/dashboard`             | Create a new dashboard              |
| PUT    | `/api/dashboard/{id}`        | Update an existing dashboard        |
| GET    | `/api/random`                | Generate a random number            |
| GET    | `/api/uuid`                  | Generate a random UUID              |
| GET    | `/{shortUrlUid}`             | Redirect to the target URL          |

## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/alkaphreak/marstech-link-spray.git
    ```

2. Configure MongoDB:
    - Ensure that MongoDB is installed and running on your machine or set up a MongoDB instance using Docker, or on a
      cloud platform, a [MongoDB Atlas free tier is available](https://www.mongodb.com/pricing).
    - Set the `MONGODB_URI_LINK_SPRAY` environment variable with the connection URI to your MongoDB database. For
      example:
        ```sh
        # On Linux/macOS, in ~/.profile or ~/.bash_profile or ...
        export MONGODB_URI_LINK_SPRAY="mongodb://localhost:27017/mydatabase"
        ```
    - Reload your shell configuration file (e.g., `~/.profile` or `~/.bash_profile`):
        ```sh
        source ~/.profile
        ```    
    - You can also need this command to avoid restarting your Mac:
        ```sh
        launchctl setenv MONGODB_URI_LINK_SPRAY "$MONGODB_URI_LINK_SPRAY"
        ```
    - Alternatively, you can set the `MONGODB_URI_LINK_SPRAY` property in the `application.properties` file.
    - You may be forced to restart your IDE to take into account the new environment variable.

3. Build and run the application:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

4. Access the application in your browser, by default at:
    ```
    http://localhost:8096
    ```

## Configuration

The application can be configured through the `application.properties` file or environment variables.

| Property / Variable                      | Description                            | Default              |
|------------------------------------------|----------------------------------------|----------------------|
| `MONGODB_URI_LINK_SPRAY`                 | MongoDB connection URI (prod profile)  | -                    |
| `server.port`                            | HTTP server port                       | `8096`               |
| `mt.link-spray.protocol`                 | Public protocol (http/https)           | `http`               |
| `mt.link-spray.host`                     | Public hostname                        | `localhost`          |
| `mt.link-spray.port`                     | Public port                            | `${server.port}`     |
| `mt.link-spray.random-id.charset`        | Character set for random ID generation | alphanumeric         |
| `mt.link-spray.random-id.length`         | Length of generated short IDs          | `5`                  |
| `mt.link-spray.random-id.cache.enabled`  | Enable pre-generated ID cache          | `true`               |
| `mt.link-spray.random-id.cache.depth`    | Number of IDs to pre-generate in cache | `100`                |
| `mt.link-spray.random-id.cache.treshold` | Threshold to trigger cache refill      | `10`                 |
| `LINK_SPRAY_SMTP_HOST`                   | SMTP server host                       | `mailtrap.io`        |
| `LINK_SPRAY_SMTP_PORT`                   | SMTP server port                       | `2525`               |
| `LINK_SPRAY_SMTP_USER`                   | SMTP username                          | -                    |
| `LINK_SPRAY_SMTP_PASSWORD`               | SMTP password                          | -                    |
| `LINK_SPRAY_SMTP_AUTH`                   | Enable SMTP authentication             | -                    |
| `LINK_SPRAY_SMTP_STARTTLS`               | Enable STARTTLS                        | -                    |
| `LINK_SPRAY_SENDER`                      | Notification sender email address      | `sender@localhost`   |
| `LINK_SPRAY_RECEIVER`                    | Notification receiver email address    | `receiver@localhost` |

### Spring Profiles

| Profile | MongoDB URI                  | Notes                     |
|---------|------------------------------|---------------------------|
| `dev`   | `localhost:27017`            | Local development         |
| `test`  | `localhost:27018` (embedded) | Integration tests         |
| `prod`  | `${MONGODB_URI_LINK_SPRAY}`  | Production (env variable) |

## Usage

1. Enter the URLs you want to open, one per line, in the text field.
2. Click the "Generate URL" button.
3. Use the generated link to open all the URLs with a single click.

## Performance

A simple test to retrieve 10 000 ids with and without the cache.

- With cache: 473ms
- Without cache: 11 327 ms
- Difference: 10 854 ms
- Test range: 10 000
- Cache depth: 100
- Cache treshold: 10

## Test containers

If you experience a message like this:

> Reuse was requested, but the environment does not support the reuse of containers
> To enable reuse of containers, you must set 'testcontainers.reuse.enable=true' in a file located at $
> HOME/.testcontainers.properties

Run this in shell or do it manually:

```shell
echo "testcontainers.reuse.enable=true" >> $HOME/.testcontainers.properties
```

## Releases

This project uses **JReleaser** for automated, professional releases. Current version: **0.0.5**

### Quick Release
1. Go to GitHub Actions → "Release" workflow
2. Click "Run workflow" and select bump type (`patch`, `minor`, or `major`)
3. Automated release with changelog and artifacts

See [RELEASE.md](RELEASE.md) for detailed release documentation.

## Contributing

Contributions are welcome! To contribute:

1. Fork the project.
2. Create a branch for your feature (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push your branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Authors

- **Stéphane ROBIN** - *Creator* - [Link](https://linktr.ee/StephaneRobinJob)

---

\© 2024 Marstech. All rights reserved.

---

## Hi there

Here is my Linktree: https://linktr.ee/StephaneRobinJob

Here is my referral link on DigitalOcean: https://m.do.co/c/be389fc15e1a

Get $200 in free DigitalOcean credits to deploy your next project! Perfect for developers starting their cloud journey.

[![DigitalOcean Referral Badge](https://web-platforms.sfo2.cdn.digitaloceanspaces.com/WWW/Badge%201.svg)](https://www.digitalocean.com/?refcode=be389fc15e1a&utm_campaign=Referral_Invite&utm_medium=Referral_Program&utm_source=badge)
