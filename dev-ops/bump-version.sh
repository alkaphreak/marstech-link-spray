#!/bin/bash

set -e

BUMP_TYPE=${1:-patch}

if [[ ! "$BUMP_TYPE" =~ ^(major|minor|patch)$ ]]; then
    echo "Usage: $0 [major|minor|patch]"
    echo "Default: patch"
    exit 1
fi

# Get current version
CURRENT=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout | sed 's/-SNAPSHOT//')
echo "Current version: $CURRENT"

# Parse version
IFS='.' read -r major minor patch <<< "$CURRENT"

# Bump version
case "$BUMP_TYPE" in
    major)
        major=$((major + 1))
        minor=0
        patch=0
        ;;
    minor)
        minor=$((minor + 1))
        patch=0
        ;;
    patch)
        patch=$((patch + 1))
        ;;
esac

NEW_VERSION="$major.$minor.$patch"
echo "New version: $NEW_VERSION"

# Update pom.xml
mvn versions:set -DnewVersion="$NEW_VERSION"

echo "Version bumped to $NEW_VERSION"
echo "Run 'git add pom.xml && git commit -m \"chore: bump version to $NEW_VERSION\"' to commit"