FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8096
# 45% of 512 MB is about 230 MB of heap: measured off-heap use is up to about 230 MB, so higher risks an OOM kill
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=45", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
