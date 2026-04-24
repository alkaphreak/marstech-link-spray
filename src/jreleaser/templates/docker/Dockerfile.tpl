FROM eclipse-temurin:21-jre-jammy

{{#labels}}
LABEL "{{labelKey}}"="{{labelValue}}"
{{/labels}}

# Install curl for the HEALTHCHECK only if not already present; clean apt lists to keep image layer small
RUN command -v curl > /dev/null 2>&1 \
    || (apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*)

WORKDIR /app
# JReleaser places artifacts under the assembly/ directory in the Docker build context
COPY assembly/{{distributionName}}-{{projectVersion}}.jar app.jar
EXPOSE 8096

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8096/actuator/health || exit 1

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
