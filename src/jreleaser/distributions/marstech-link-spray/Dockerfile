FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY {{distributionName}}-{{projectEffectiveVersion}}.jar app.jar

EXPOSE 8096

ENTRYPOINT ["java", "-jar", "app.jar"]

