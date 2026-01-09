#!/bin/bash
set -euo pipefail

# Display help
usage() {
    echo "Usage: $0 [options]"
    echo "Options:"
    echo "  --dry-run           Run JReleaser in dry-run mode (no publishing)"
    echo "  --help              Display this help message"
    exit 1
}

DRY_RUN=false

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --dry-run) DRY_RUN=true ;;
        --help) usage ;;
        *) echo "Unknown parameter: $1"; usage ;;
    esac
    shift
done

# Check prerequisites
if [[ -z "${GITHUB_TOKEN:-}" ]]; then
    echo "Error: GITHUB_TOKEN environment variable is not set."
    exit 1
fi

if [[ -z "${DOCKER_PASSWORD:-}" ]]; then
    echo "Warning: DOCKER_PASSWORD environment variable is not set. Docker release might fail if authentication is required."
fi

# Verify required environment variables
if [[ -z "${DOCKER_PASSWORD:-}" ]]; then
    echo "Error: DOCKER_PASSWORD is not set. Cannot proceed with Docker release."
    exit 1
else
    echo "DOCKER_PASSWORD is set"
fi

# Check if Docker is running, attempt to start if not (macOS)
if ! docker info > /dev/null 2>&1; then
    echo "Docker daemon is not running. Attempting to start..."
    if [[ "$OSTYPE" == "darwin"* ]]; then
        open -a Docker
        echo "Waiting for Docker to start..."
        while ! docker info > /dev/null 2>&1; do
            echo -n "."
            sleep 1
        done
        echo ""
        echo "Docker started successfully!"
    else
        echo "Error: Docker daemon is not running and auto-start is only supported on macOS."
        exit 1
    fi
fi

# Navigate to project root
cd "$(dirname "$(realpath "$0")")/.."

echo "----------------------------------------------------------------"
echo "Starting Release Process..."
echo "Dry Run Mode: $DRY_RUN"
echo "----------------------------------------------------------------"

# Build the project (skip tests to speed up release if CI already verified)
echo "Building project..."
mvn -B -ntp clean package -DskipTests

# Test JReleaser configuration
echo "Validating JReleaser model..."
mvn -q -B -ntp jreleaser:config \
  -Djreleaser.config.file=jreleaser.yml

if [ "$DRY_RUN" = true ]; then
    echo "Performing DRY-RUN release..."
    mvn -B -ntp jreleaser:full-release \
      -Djreleaser.config.file=jreleaser.yml \
      -Djreleaser.dry.run=true \
      -Djreleaser.strict=true
else
    echo "Performing LIVE release..."
    export JRELEASER_DOCKER_DEFAULT_PASSWORD="${DOCKER_PASSWORD}"
    mvn -B -ntp jreleaser:full-release \
      -Djreleaser.config.file=jreleaser.yml \
      -Djreleaser.strict=true
fi

echo "----------------------------------------------------------------"
echo "Release process completed successfully!"
