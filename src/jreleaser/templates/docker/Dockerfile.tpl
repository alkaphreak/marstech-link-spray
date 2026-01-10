FROM eclipse-temurin:21-jre-jammy

{{#labels}}
LABEL "{{labelKey}}"="{{labelValue}}"
{{/labels}}

WORKDIR /app
# JReleaser places artifacts under the assembly/ directory in the Docker build context
COPY assembly/{{distributionName}}-{{projectVersion}}.jar app.jar
EXPOSE 8096

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
