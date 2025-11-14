# 🎨 Quick Start - Viewing Your Release Workflow Diagrams

## Instant View (No Tools Needed)

### ASCII Diagram - View Right Now!
```bash
cat docs/release-workflow-ascii.txt
# or
less docs/release-workflow-ascii.txt
```

This gives you a complete visual flow chart in pure text - works everywhere!

---

## View on GitHub

### Mermaid Diagram - Automatically Renders!

1. Push these files to GitHub:
   ```bash
   git add docs/
   git commit -m "docs: add release workflow diagrams"
   git push
   ```

2. Navigate to: `docs/release-workflow.mmd` on GitHub
3. GitHub automatically renders the Mermaid diagram! 🎉

---

## Generate Professional Images (Optional)

### Install Graphviz
```bash
brew install graphviz
```

### Generate Images
```bash
# PNG (for documentation, presentations)
dot -Tpng docs/release-workflow.dot -o docs/release-workflow.png

# SVG (scalable, best for web)
dot -Tsvg docs/release-workflow.dot -o docs/release-workflow.svg

# PDF (for printing)
dot -Tpdf docs/release-workflow.dot -o docs/release-workflow.pdf

# Generate all formats at once
cd docs
for fmt in png svg pdf; do 
  dot -T$fmt release-workflow.dot -o release-workflow.$fmt
done
```

---

## Use in Documentation

### Embed in Markdown
```markdown
# Release Workflow

![Release Workflow](docs/release-workflow.png)

For interactive diagram, see [release-workflow.mmd](docs/release-workflow.mmd)
```

### View in IDE

**IntelliJ IDEA:**
1. Install "Graphviz (dot) language support" plugin
2. Open `release-workflow.dot`
3. Right-click → Preview

**VS Code:**
1. Install "Graphviz Preview" extension
2. Open `release-workflow.dot`
3. Ctrl+Shift+V (or Cmd+Shift+V on Mac)

---

## Online Viewers (No Installation)

### For DOT files:
- https://dreampuf.github.io/GraphvizOnline/
- https://edotor.net/
- http://viz-js.com/

### For Mermaid files:
- https://mermaid.live/

Just copy the file contents and paste!

---

## What Each Format Is Best For

| Format | Best For | Pros |
|--------|----------|------|
| **ASCII** | Terminal, text files, email | Works everywhere, no tools needed |
| **Mermaid** | GitHub, GitLab, documentation | Auto-renders, version control friendly |
| **DOT** | Professional documents, printing | High quality, highly customizable |

---

## Quick Tips

✅ **Want to view NOW?** → Use ASCII (`cat docs/release-workflow-ascii.txt`)

✅ **Want to share on GitHub?** → Push and view Mermaid (`.mmd` file)

✅ **Want high-quality images?** → Install Graphviz and generate from DOT

✅ **Want to edit?** → All formats are plain text, use any editor

---

**Current Status:**
- ✅ ASCII diagram ready to view
- ✅ Mermaid diagram ready for GitHub
- ✅ DOT diagram ready for image generation
- ✅ Documentation in `docs/README.md`

**Next Step:** 
```bash
cat docs/release-workflow-ascii.txt
```

