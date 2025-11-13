# Documentation

This folder contains documentation and diagrams for the MarsTech Link Spray project.

## Release Workflow Diagrams

### Available Formats

1. **`release-workflow.dot`** - Graphviz DOT file (most detailed, requires Graphviz)
2. **`release-workflow.mmd`** - Mermaid diagram (renders on GitHub, in IDEs with plugins)
3. **`release-workflow-ascii.txt`** - ASCII art diagram (view anywhere, no tools needed)

### Viewing the Diagrams

#### Quick View (No Installation Required)

**ASCII Diagram:**
```bash
cat docs/release-workflow-ascii.txt
# or
less docs/release-workflow-ascii.txt
```

**Mermaid Diagram:**
- View on GitHub: Open `release-workflow.mmd` - it renders automatically!
- Or copy to [Mermaid Live Editor](https://mermaid.live/)

**DOT Diagram:**
1. Copy the contents of `release-workflow.dot`
2. Paste into one of these online viewers:
   - [Graphviz Online](https://dreampuf.github.io/GraphvizOnline/)
   - [Edotor](https://edotor.net/)
   - [Viz.js](http://viz-js.com/)

#### Option 2: Generate Images Locally

**Install Graphviz:**

```bash
# macOS
brew install graphviz

# Linux (Ubuntu/Debian)
sudo apt-get install graphviz

# Linux (Fedora)
sudo dnf install graphviz
```

**Generate diagrams:**

```bash
# Generate PNG (high quality)
dot -Tpng docs/release-workflow.dot -o docs/release-workflow.png

# Generate SVG (scalable, best for web)
dot -Tsvg docs/release-workflow.dot -o docs/release-workflow.svg

# Generate PDF
dot -Tpdf docs/release-workflow.dot -o docs/release-workflow.pdf

# Generate all formats at once
cd docs
dot -Tpng release-workflow.dot -o release-workflow.png
dot -Tsvg release-workflow.dot -o release-workflow.svg
dot -Tpdf release-workflow.dot -o release-workflow.pdf
```

#### Option 3: IDE Plugins

**IntelliJ IDEA / JetBrains IDEs:**
1. Install "Graphviz (dot) language support" plugin
2. Open `release-workflow.dot`
3. Right-click → "Open in Browser" or use preview pane

**VS Code:**
1. Install "Graphviz Preview" or "Graphviz (dot) language support" extension
2. Open `release-workflow.dot`
3. Use preview command (Ctrl+Shift+V or Cmd+Shift+V)

### Diagram Contents

The release workflow diagram includes:

- 🚀 **Trigger Point** - Manual GitHub Actions workflow dispatch
- 👤 **User Input** - Version bump type selection (patch/minor/major)
- 📦 **Checkout & Setup** - Code checkout and Java environment setup
- 🔢 **Version Management** - Automated version bumping
- 🌿 **Branch Management** - Release branch creation
- 🔨 **Build & Test** - Maven build and verification
- 📋 **JReleaser Steps**:
  - Git tag creation
  - Changelog generation
  - GitHub release creation
  - Docker image building
  - Docker Hub publishing
- 🔄 **Main Branch Update** - Automatic dev version bump
- ✅ **Success State** - Final release completion
- 📦 **Artifacts** - Generated release artifacts
- 🔐 **Required Secrets** - Environment configuration

### Color Coding

- 🟢 **Green** - Success states and positive outcomes
- 🔵 **Blue** - Processing steps and information
- 🟡 **Yellow** - Setup and configuration steps
- 🟣 **Purple** - JReleaser-specific operations
- 🔴 **Red** - Decision points and failure states
- 💡 **Light Yellow** - Informational notes and artifacts

### Updating the Diagram

To modify the workflow diagram:

1. Edit `release-workflow.dot`
2. Follow DOT language syntax: [Graphviz Documentation](https://graphviz.org/documentation/)
3. Regenerate images using the commands above
4. Commit both `.dot` file and generated images

### Quick Reference

```bash
# Quick generate all formats
make diagrams  # If Makefile is set up

# Or use this one-liner:
for fmt in png svg pdf; do dot -T$fmt docs/release-workflow.dot -o docs/release-workflow.$fmt; done
```

## Additional Documentation

- **[RELEASE_GUIDE.md](../RELEASE_GUIDE.md)** - Complete release process guide
- **[RELEASE.md](../RELEASE.md)** - Quick release instructions
- **[CHANGELOG.md](../CHANGELOG.md)** - Project changelog

---

**Last Updated:** November 6, 2025

