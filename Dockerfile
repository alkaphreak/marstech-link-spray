FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
