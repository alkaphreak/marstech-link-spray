FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8096
# Default heap is 25% of the container RAM (about 128 MB on the 512 MB DO instance)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
