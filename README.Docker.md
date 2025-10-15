### Building and running your application

When you're ready, start your application by running:
`docker compose up --build`.

Your application will be available at http://localhost:8096.

### Deploying your application to the cloud

First, build your image, e.g.: `docker build -t myapp .`.
If your cloud uses a different CPU architecture than your development
machine (e.g., you are on a Mac M1 and your cloud provider is amd64),
you'll want to build the image for that platform, e.g.:
`docker build --platform=linux/amd64 -t myapp .`.

Then, push it to your registry, e.g. `docker push myregistry.com/myapp`.

Consult Docker's [getting started](https://docs.docker.com/go/get-started-sharing/)
docs for more detail on building and pushing.

## Local vs JReleaser Dockerfile Usage

This project uses two Dockerfiles:

- **Root Dockerfile**: For local builds, uses `COPY target/*.jar app.jar`.
- **JReleaser Dockerfile**: Located at `src/jreleaser/distributions/marstech-link-spray/Dockerfile`, uses JReleaser template tokens and is used only by JReleaser during release packaging.

### Local Build
To build and run locally:
```
docker build -t myapp .
docker run -p 8096:8096 myapp
```

### JReleaser Build
JReleaser will use its own Dockerfile and artifact naming conventions. See `jreleaser.yml` for configuration.

For more details, see the [JReleaser documentation](https://jreleaser.org/guide/latest/).
