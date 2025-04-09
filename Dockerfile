FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn ./.mvn
COPY src ./src

RUN ./mvnw clean install

CMD ["java", "-jar", "target/*.jar"]
