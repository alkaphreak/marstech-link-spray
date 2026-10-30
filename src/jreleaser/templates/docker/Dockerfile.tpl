FROM eclipse-temurin:25-jre-jammy

{{#labels}}
LABEL "{{labelKey}}"="{{labelValue}}"
{{/labels}}

# Install curl for the HEALTHCHECK only if not already present; clean apt lists to keep image layer small
RUN command -v curl > /dev/null 2>&1 \
    || (apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*)

# Tesseract OCR engine with its French and English data, for /api/ocr (MLS-252)
RUN apt-get update && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-fra \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
# DB-IP Lite country and ASN databases for the IP tools (CC BY 4.0, MLS-113), refreshed at each release.
# A month's file is published early in the month: fall back to the previous month's.
RUN mkdir geoip && for db in country asn; do \
      for month in "$(date +%Y-%m)" "$(date -d "$(date +%Y-%m-01) -1 month" +%Y-%m)"; do \
        curl -fsSL --retry 3 "https://download.db-ip.com/free/dbip-$db-lite-$month.mmdb.gz" \
          | gunzip > geoip/part && mv geoip/part "geoip/dbip-$db-lite.mmdb" && break; \
      done; \
      test -s "geoip/dbip-$db-lite.mmdb" || exit 1; \
    done && rm -f geoip/part
# JReleaser places artifacts under the assembly/ directory in the Docker build context
COPY assembly/{{distributionName}}-{{projectVersion}}.jar app.jar
EXPOSE 8096

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8096/actuator/health || exit 1

# 45% of 512 MB is about 230 MB of heap: measured off-heap use is up to about 230 MB, so higher risks an OOM kill
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=45", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
