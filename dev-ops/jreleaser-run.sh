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
# JReleaser reads JRELEASER_GITHUB_TOKEN; accept GITHUB_TOKEN as a fallback
export JRELEASER_GITHUB_TOKEN="${JRELEASER_GITHUB_TOKEN:-${GITHUB_TOKEN:-}}"
if [[ -z "${JRELEASER_GITHUB_TOKEN}" ]]; then
    echo "Error: neither JRELEASER_GITHUB_TOKEN nor GITHUB_TOKEN is set."
    exit 1
fi

# Docker Hub account that owns the alkaphreak/marstech-link-spray images (jreleaser.yml imageNames)
export JRELEASER_DOCKER_DEFAULT_USERNAME="${JRELEASER_DOCKER_DEFAULT_USERNAME:-${DOCKER_HUB_USERNAME:-${DOCKER_USERNAME:-alkaphreak}}}"

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
