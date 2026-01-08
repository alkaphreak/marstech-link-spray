#!/bin/bash
set -euo pipefail

# Navigate to project root
cd "$(dirname "$(realpath "$0")")/.."

echo "Running JReleaser..."

# Build the project
echo "Building project..."
mvn clean package -DskipTests

# Test JReleaser configuration (validates config; does not require a real token)
echo "Testing JReleaser configuration..."
export JRELEASER_GITHUB_TOKEN=$GITHUB_TOKEN
mvn jreleaser:config

# Perform a full release using JReleaser
echo "Performing full release with JReleaser..."
mvn -ntp \
  jreleaser:full-release \
  -Djreleaser.github.token="$GITHUB_TOKEN"
