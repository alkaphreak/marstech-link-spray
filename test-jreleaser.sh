#!/bin/bash

echo "Testing JReleaser setup..."

# Build the project
echo "Building project..."
mvn clean package -DskipTests

# Test JReleaser configuration (this will fail due to missing token, but shows config is valid)
echo "Testing JReleaser configuration..."
export JRELEASER_GITHUB_TOKEN="dummy-token-for-testing"
mvn jreleaser:config

echo "JReleaser setup test completed!"
echo ""
echo "To use JReleaser for real releases:"
echo "1. Set JRELEASER_GITHUB_TOKEN environment variable with your GitHub token"
echo "2. Run: mvn jreleaser:full-release"
echo "3. Or use the GitHub Actions workflow in .github/workflows/release.yml"