# Marstech - Link Spray

**Marstech - Link Spray** is a web application that allows you to open multiple URLs with a single click. Follow a few
simple steps to generate a unique link that will open all the specified URLs.

## Badges

[![SonarQube](https://github.com/alkaphreak/marstech-link-spray/actions/workflows/maven-sonar.yml/badge.svg)](https://github.com/alkaphreak/marstech-link-spray/actions/workflows/maven-sonar.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alkaphreak_marstech-link-spray&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alkaphreak_marstech-link-spray)

## Features

- **Unique link generation**: Enter multiple URLs and generate a single link to open them all at once.
- **Intuitive user interface**: A simple and easy-to-use interface to enter your URLs and generate the link.

## Technologies used

- **Java 21**
- **Spring Boot**
- **Thymeleaf**
- **Bootstrap**
- **Maven**
- **MongoDB**
- **Docker** (for testing purposes)

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

The application can be configured using through the `application.properties` file or environment variables.

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

This project uses **JReleaser** for automated, professional releases.

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

\© 2021 Marstech. All rights reserved.

---

## Hi there 👋

Here is my Linktree: https://linktr.ee/StephaneRobinJob

Here is my refreal link on digital ocean: https://m.do.co/c/be389fc15e1a

Get $200 in free DigitalOcean credits to deploy your next project! Perfect for developers starting their cloud journey.

[![DigitalOcean Referral Badge](https://web-platforms.sfo2.cdn.digitaloceanspaces.com/WWW/Badge%201.svg)](https://www.digitalocean.com/?refcode=be389fc15e1a&utm_campaign=Referral_Invite&utm_medium=Referral_Program&utm_source=badge)
