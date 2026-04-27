# Release Process with JReleaser

This project uses JReleaser for automated, professional releases.

## Quick Start

### Automated Release (Recommended)

1. Go to GitHub Actions in your repository
2. Select "Release" workflow
3. Click "Run workflow"
4. Select version bump type (patch/minor/major)
5. Click "Run workflow"

The workflow will:
- Set the version in pom.xml
- Build and test the project
- Create a GitHub release with changelog
- Upload JAR artifacts with checksums
- Build and push Docker images to Docker Hub

### Manual Release

```bash
# Bump version (patch/minor/major)
./dev-ops/bump-version.sh patch

# Build project
mvn clean package

# Set GitHub token
export JRELEASER_GITHUB_TOKEN="your-github-token"

# Create release
mvn jreleaser:full-release
```

## Version Management

### Automated Semantic Versioning
- **Patch** (0.0.1 → 0.0.2): Bug fixes, small changes
- **Minor** (0.1.0 → 0.2.0): New features, backward compatible
- **Major** (1.0.0 → 2.0.0): Breaking changes

### Local Development
```bash
# Bump patch version
./dev-ops/bump-version.sh patch

# Bump minor version
./dev-ops/bump-version.sh minor

# Bump major version
./dev-ops/bump-version.sh major
```

## Configuration

JReleaser is configured in `.github/jreleaser.yml`:

- **GitHub Releases**: Automatic tag and release creation
- **Changelog**: Generated from conventional commits
- **Artifacts**: JAR files with checksums
- **Security**: Artifact signing (optional)

## Release Features

- ✅ Automated GitHub releases
- ✅ Professional changelog generation
- ✅ JAR artifact distribution
- ✅ Checksum generation
- ✅ Conventional commit parsing
- ✅ Version management
- ✅ GitHub Actions integration
- ✅ Docker Hub publishing
- ✅ Multi-tag Docker images (version + latest)

## Docker Hub Setup

### Required Secrets
Add these to your GitHub repository secrets:
- `DOCKER_HUB_USERNAME` - Your Docker Hub username
- `DOCKER_HUB_ACCESS_TOKEN` - Docker Hub access token

### Repository Variables (Optional)
- `DOCKER_REPO` - Docker repository name (defaults to 'marstech-link-spray')

### Docker Images
Releases create two Docker tags:
- `username/repo:v1.0.0` (version tag)
- `username/repo:latest` (latest tag)

## Troubleshooting

### Missing GitHub Token
```
ERROR: release.github.token must not be blank
```
**Solution**: Set `JRELEASER_GITHUB_TOKEN` environment variable

### Docker Hub Authentication
```
ERROR: Docker login failed
```
**Solution**: Check `DOCKER_HUB_USERNAME` and `DOCKER_HUB_ACCESS_TOKEN` secrets

### Artifact Not Found
```
ERROR: Artifact not found: target/mt-link-spray-x.x.x.jar
```
**Solution**: Run `mvn clean package` first

### Test Configuration
```bash
# Test without releasing
mvn jreleaser:config

# Dry run
mvn jreleaser:release -Djreleaser.dry.run=true
```

## Automated Workflow

1. **Development**: Work with SNAPSHOT versions (e.g., `0.0.5-SNAPSHOT`)
2. **Release**: Choose bump type → Automated version calculation
3. **Post-Release**: Automatically updates to next SNAPSHOT version

Example progression:
- Current: `0.0.4-SNAPSHOT`
- Release (patch): `0.0.4`
- Next dev: `0.0.5-SNAPSHOT`