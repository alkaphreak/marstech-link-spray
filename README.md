# Marstech - Link Spray

**Marstech - Link Spray** is a web application that allows you to open multiple URLs with a single click. Follow a few
simple steps to generate a unique link that will open all the specified URLs.

## Features

- **Unique link generation**: Enter multiple URLs and generate a single link to open them all at once.
- **Intuitive user interface**: A simple and easy-to-use interface to enter your URLs and generate the link.

## Technologies used

- **Java 21**
- **Kotlin**
- **Spring Boot**
- **Thymeleaf**
- **Bootstrap**
- **Maven**
- **MongoDB**
- **Docker**
- **Testcontainers**
- **Apache Commons**
- **Jakarta Servlet API**
- **Lombok**

## Installation

1. Clone the repository:
    ```bash
    git clone https://github.com/alkaphreak/marstech-link-spray.git
    ```

2. Configure MongoDB:
    - Ensure that MongoDB is installed and running on your machine or set up a MongoDB instance using Docker, or on a
      cloud platform, a [MongoDB Atlas free tier is available](https://www.mongodb.com/pricing).
    - Set the `MONGODB_URI` environment variable with the connection URI to your MongoDB database. For example:
        ```sh
        # On Linux/macOS, in ~/.profile or ~/.bash_profile or ...
        export MONGODB_URI="mongodb://localhost:27017/mydatabase"
        ```
    - Reload your shell configuration file (e.g., `~/.profile` or `~/.bash_profile`):
        ```sh
        source ~/.profile
        ```    
    - You can also need this command to avoid restarting your Mac:
        ```sh
        launchctl setenv MONGODB_URI "$MONGODB_URI"
        ```
    - Alternatively, you can set the `MONGODB_URI` property in the `application.properties` file.
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
- Without cache: 11 327ms
- Difference: 10 854ms
- Test range: 10 000
- Cache depth: 100
- Cache treshold: 10

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

- **Stéphane ROBIN** - *Creator* - [Marstech](https://marstech.fr/)

---

\© 2021 Marstech. All rights reserved.