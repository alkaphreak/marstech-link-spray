# 📦 MarsTech Link Spray - Complete Release Guide

> **Current Version**: 0.0.4-SNAPSHOT  
> **Last Updated**: November 6, 2025

---

## 🎯 Overview

This project uses **JReleaser** for automated releases with GitHub Actions integration. The release workflow handles:
- ✅ Version bumping (semantic versioning)
- ✅ Building and testing
- ✅ Creating GitHub releases with changelog
- ✅ Publishing JAR artifacts
- ✅ Building and pushing Docker images to Docker Hub
- ✅ Automatic branch management

---

## 🚀 Quick Start: Automated Release (Recommended)

### Prerequisites
Before your first release, ensure you have configured:

1. **GitHub Secrets** (Settings → Secrets and variables → Actions):
   - `DOCKER_HUB_USERNAME` - Your Docker Hub username
   - `DOCKER_HUB_ACCESS_TOKEN` - Docker Hub access token ([Create one here](https://hub.docker.com/settings/security))
   - `GITHUB_TOKEN` - Automatically provided by GitHub Actions

2. **GitHub Repository Variables** (Optional):
   - `DOCKER_REPO` - Docker repository name (defaults to `marstech-link-spray`)

### Release Steps

1. **Navigate to GitHub Actions**
   - Go to your repository on GitHub
   - Click on the **"Actions"** tab
   - Select **"Release"** workflow from the left sidebar

2. **Trigger the Release**
   - Click **"Run workflow"** button (top right)
   - Select the branch: `main`
   - Choose version bump type:
     - **`patch`** (0.0.4 → 0.0.5) - Bug fixes, small changes
     - **`minor`** (0.0.4 → 0.1.0) - New features, backward compatible
     - **`major`** (0.0.4 → 1.0.0) - Breaking changes
   - Click **"Run workflow"**

3. **Monitor the Release**
   - Watch the workflow execution
   - The workflow will take 5-10 minutes to complete
   - Check for any errors in the logs

### What Happens Automatically

The GitHub Actions workflow will:

1. **Checkout Code** from the main branch
2. **Setup Java 21** (Temurin distribution)
3. **Bump Version** using `dev-ops/bump-version.sh`
4. **Create Release Branch** (e.g., `release/v0.0.5`)
5. **Build and Test** the project with Maven
6. **Run JReleaser** which:
   - Creates a Git tag (e.g., `v0.0.5`)
   - Generates changelog from commits
   - Creates GitHub Release with artifacts
   - Builds Docker image
   - Pushes to Docker Hub with version and `latest` tags
7. **Update Main Branch** to next development version (e.g., `0.0.6-SNAPSHOT`)

---

## 🛠️ Manual Release (Advanced)

### Local Development Release

```bash
# 1. Ensure you're on main branch and up to date
git checkout main
git pull origin main

# 2. Bump version (choose one: patch, minor, major)
./dev-ops/bump-version.sh patch

# 3. Commit the version change
NEW_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
git add pom.xml
git commit -m "chore: bump version to $NEW_VERSION for release"

# 4. Build and test
mvn clean verify

# 5. Set up GitHub token
export JRELEASER_GITHUB_TOKEN="your-github-personal-access-token"

# 6. Optional: Set up Docker Hub credentials (for Docker image push)
export DOCKER_HUB_USERNAME="your-dockerhub-username"
export DOCKER_HUB_ACCESS_TOKEN="your-dockerhub-token"

# 7. Run JReleaser
mvn jreleaser:full-release

# 8. Push changes and tags
git push origin main
git push origin v$NEW_VERSION

# 9. Update to next development version
./dev-ops/bump-version.sh patch
mvn versions:set -DnewVersion=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)-SNAPSHOT
git add pom.xml
git commit -m "chore: bump to next development version"
git push origin main
```

### Creating GitHub Personal Access Token

For manual releases, you need a GitHub token with these permissions:
- `repo` (Full control of private repositories)
- `write:packages` (Upload packages to GitHub Package Registry)

Create one at: https://github.com/settings/tokens

---

## 📋 Version Management

### Semantic Versioning Strategy

This project follows [Semantic Versioning](https://semver.org/):

```
MAJOR.MINOR.PATCH

Example: 1.2.3
         │ │ │
         │ │ └─── PATCH: Bug fixes (backwards compatible)
         │ └───── MINOR: New features (backwards compatible)
         └─────── MAJOR: Breaking changes
```

### When to Use Each Bump Type

**Patch (0.0.4 → 0.0.5)**
- Bug fixes
- Documentation updates
- Performance improvements
- Refactoring without API changes
- Security patches

**Minor (0.0.4 → 0.1.0)**
- New features
- New API endpoints
- Enhancements to existing features
- Deprecations (with backward compatibility)

**Major (0.0.4 → 1.0.0)**
- Breaking API changes
- Removal of deprecated features
- Major architectural changes
- Framework version upgrades with breaking changes

### Development Version

Between releases, the project uses `-SNAPSHOT` versions (e.g., `0.0.5-SNAPSHOT`):
- Indicates work in progress
- Not suitable for production
- Automatically updated after each release

---

## 🐳 Docker Publishing

### Docker Hub Configuration

Releases automatically build and push Docker images to Docker Hub.

**Image Tags Created:**
- `username/marstech-link-spray:v0.0.5` (version-specific)
- `username/marstech-link-spray:latest` (always points to latest release)

**Dockerfile Location:**
- `src/jreleaser/distributions/marstech-link-spray/Dockerfile`

### Pulling Released Docker Images

```bash
# Pull specific version
docker pull yourusername/marstech-link-spray:v0.0.5

# Pull latest version
docker pull yourusername/marstech-link-spray:latest

# Run the container
docker run -p 8080:8080 \
  -e MONGODB_URI_LINK_SPRAY="mongodb://host.docker.internal:27017/linkspray" \
  yourusername/marstech-link-spray:latest
```

---

## 📝 Changelog Management

### Automatic Changelog Generation

JReleaser generates changelogs from your Git commit messages using [Conventional Commits](https://www.conventionalcommits.org/).

**Commit Message Format:**
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Common Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `chore`: Maintenance tasks
- `refactor`: Code refactoring
- `test`: Test updates
- `perf`: Performance improvements

**Examples:**
```bash
git commit -m "feat(api): add pastebin service endpoint"
git commit -m "fix(security): resolve XSS vulnerability in input validation"
git commit -m "docs: update release guide with Docker instructions"
git commit -m "chore: bump version to 0.0.5"
```

### Manual Changelog Updates

The project maintains `CHANGELOG.md` which should be manually updated for significant releases:

```bash
# Edit CHANGELOG.md
nano CHANGELOG.md

# Add your changes under the new version section
## [0.0.5] - 2025-11-06

### Added
- New pastebin service feature
- API endpoint for paste operations

### Fixed
- Security vulnerability in input validation
```

---

## 🔍 Testing the Release Workflow

### Dry Run (No Actual Release)

Test JReleaser configuration without creating a release:

```bash
# Build the project
mvn clean package -DskipTests

# Test configuration (validates setup)
./test-jreleaser.sh

# Or manually:
export JRELEASER_GITHUB_TOKEN="dummy-token-for-testing"
mvn jreleaser:config
```

---

## 🎨 Release Workflow Customization

### Modifying the GitHub Actions Workflow

**File:** `.github/workflows/release.yml`

**Common Customizations:**

1. **Add Slack/Discord Notifications**
   ```yaml
   - name: Notify on Release
     if: success()
     run: |
       curl -X POST -H 'Content-type: application/json' \
         --data '{"text":"Release ${{ steps.bump-version.outputs.version }} completed!"}' \
         ${{ secrets.SLACK_WEBHOOK_URL }}
   ```

2. **Run Additional Tests**
   ```yaml
   - name: Integration Tests
     run: mvn -B -ntp verify -Pintegration-tests
   ```

3. **Deploy to Additional Registries**
   ```yaml
   - name: Push to GHCR
     run: |
       echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
       docker tag marstech-link-spray:latest ghcr.io/${{ github.repository }}:latest
       docker push ghcr.io/${{ github.repository }}:latest
   ```

### Modifying JReleaser Configuration

**File:** `jreleaser.yml`

**Common Customizations:**

1. **Add More Docker Registries**
   ```yaml
   distributions:
     marstech-link-spray:
       type: JAVA_BINARY
       artifact:
         path: target/marstech-link-spray-{{projectEffectiveVersion}}.jar
       docker:
         active: ALWAYS
         registries:
           - serverName: docker.io
           - serverName: ghcr.io
   ```

2. **Configure Artifact Signing**
   ```yaml
   signing:
     active: ALWAYS
     armored: true
   ```

---

## 🐛 Troubleshooting

### Common Issues and Solutions

#### Issue: "release.github.token must not be blank"
**Solution:**
```bash
export JRELEASER_GITHUB_TOKEN="your-github-token"
```
Or ensure GitHub Actions has `GITHUB_TOKEN` secret configured.

#### Issue: Docker Hub authentication failed
**Solution:**
1. Verify Docker Hub credentials in GitHub Secrets
2. Ensure access token has `Read, Write, Delete` permissions
3. Check token hasn't expired

```bash
# Test Docker Hub login locally
echo "$DOCKER_HUB_ACCESS_TOKEN" | docker login -u "$DOCKER_HUB_USERNAME" --password-stdin
```

#### Issue: Version conflict or tag already exists
**Solution:**
```bash
# Delete local tag
git tag -d v0.0.5

# Delete remote tag
git push origin :refs/tags/v0.0.5

# Re-run release
```

#### Issue: Build fails during release
**Solution:**
```bash
# Test build locally first
mvn clean verify

# Check for compilation errors
mvn clean compile

# Run tests separately
mvn test
```

#### Issue: Release branch not merging
**Solution:**
- Check for merge conflicts in GitHub
- Manually merge release branch if needed:
```bash
git checkout main
git merge release/v0.0.5
git push origin main
```

---

## 📊 Release Checklist

Before triggering a release, ensure:

- [ ] All tests pass locally: `mvn clean verify`
- [ ] Code is committed and pushed to `main` branch
- [ ] `CHANGELOG.md` is updated (if doing manual updates)
- [ ] GitHub Secrets are configured (for first release)
- [ ] Docker Hub credentials are valid
- [ ] No open critical bugs or security issues
- [ ] Documentation is up to date
- [ ] Branch protection rules allow the workflow to push

After release:

- [ ] Verify GitHub Release was created: https://github.com/alkaphreak/marstech-link-spray/releases
- [ ] Check Docker image on Docker Hub
- [ ] Test the Docker image locally
- [ ] Update any external documentation referencing the version
- [ ] Announce the release (if applicable)

---

## 🔗 Useful Commands

```bash
# Check current version
mvn help:evaluate -Dexpression=project.version -q -DforceStdout

# List all tags
git tag -l

# View last 10 commits
git log --oneline -10

# Check if working directory is clean
git status

# View release workflow runs
# Visit: https://github.com/alkaphreak/marstech-link-spray/actions/workflows/release.yml

# Build without tests (faster)
mvn clean package -DskipTests

# Run only specific test
mvn test -Dtest=YourTestClass

# Check for dependency updates
mvn versions:display-dependency-updates

# Clean all build artifacts
mvn clean
rm -rf target/
```

---

## 📚 Additional Resources

- **JReleaser Documentation**: https://jreleaser.org/guide/latest/
- **Semantic Versioning**: https://semver.org/
- **Conventional Commits**: https://www.conventionalcommits.org/
- **GitHub Actions**: https://docs.github.com/en/actions
- **Docker Hub**: https://hub.docker.com/
- **Maven Release Plugin**: https://maven.apache.org/maven-release/maven-release-plugin/

---

## 🆘 Getting Help

If you encounter issues not covered in this guide:

1. Check GitHub Actions logs for detailed error messages
2. Review JReleaser documentation for configuration options
3. Search existing GitHub issues
4. Create a new issue with:
   - Error message
   - Workflow run link
   - Steps to reproduce

---

**Happy Releasing! 🎉**

