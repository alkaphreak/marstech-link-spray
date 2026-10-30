FROM eclipse-temurin:25-jre-jammy
# Tesseract OCR engine with its French and English data, for /api/ocr (MLS-252)
RUN apt-get update && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-fra \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8096
# 45% of 512 MB is about 230 MB of heap: measured off-heap use is up to about 230 MB, so higher risks an OOM kill
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=45", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
