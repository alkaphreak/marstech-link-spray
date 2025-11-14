# 📝 GitHub Copilot Context Configuration Guide

## Where to Write Context for GitHub Copilot in IntelliJ IDEA

I've created context files for your project. Here are **all the locations** where Copilot reads context:

---

## ✅ 1. Repository-Level Instructions (Recommended - CREATED)

**Location**: `.github/copilot-instructions.md`

**Purpose**: Project-wide coding standards, patterns, and conventions that Copilot uses for ALL suggestions in this repository.

**What Copilot reads**:
- Coding style preferences
- Framework conventions
- Architecture patterns
- Testing approaches
- Technology stack details

**✅ I've created this file for you** with:
- Kotlin preference over Java
- Spring Boot conventions
- MongoDB patterns
- Testing guidelines
- Project-specific patterns

**Status**: ✅ **CREATED** at `.github/copilot-instructions.md`

---

## ✅ 2. Workspace-Level Instructions (CREATED)

**Location**: `.idea/copilot-instructions.md`

**Purpose**: IntelliJ IDEA workspace-specific context, quick references, and patterns.

**What Copilot reads**:
- Quick command references
- Common code patterns
- Workspace-specific preferences

**✅ I've created this file for you** with:
- Quick reference patterns
- Common commands
- Code generation templates

**Status**: ✅ **CREATED** at `.idea/copilot-instructions.md`

---

## 📄 3. README Files (Already Exists)

**Locations**: 
- `README.md` (root)
- `docs/README.md`
- Any README in subdirectories

**Purpose**: Copilot automatically reads README files to understand project context.

**What to include**:
- Project description
- Setup instructions
- Architecture overview
- Key features

**Status**: ✅ Already exists in your project

---

## 💬 4. Inline Comments in Code

**Location**: Any `.kt`, `.java`, or other source file

**Purpose**: File-specific context for Copilot when working in that file.

**Example**:
```kotlin
// Copilot: When generating code in this file:
// - Use reactive patterns
// - Follow the repository pattern established here
// - Apply MongoDB indexing best practices

@Repository
interface UserRepository : MongoRepository<User, String> {
    // ...
}
```

**Usage**: Write these at the top of files where you want specific behavior.

---

## 🎯 5. IntelliJ IDEA Settings

**Location**: Settings → Tools → GitHub Copilot

**Access**: 
1. Open IntelliJ IDEA
2. Go to: **File → Settings** (Windows/Linux) or **IntelliJ IDEA → Settings** (Mac)
3. Navigate to: **Tools → GitHub Copilot**

**What you can configure**:
- Enable/disable Copilot
- Language preferences
- Suggestion behavior
- Copilot Chat settings

---

## 🔧 6. EditorConfig (Optional)

**Location**: `.editorconfig`

**Purpose**: Code style preferences (indentation, line endings, etc.)

**Example**:
```editorconfig
[*.kt]
indent_style = space
indent_size = 4
```

**Status**: ⚠️ Not created (optional - your IDE already has Kotlin formatting)

---

## 📋 7. File-Level Copilot Instructions

**Location**: Top of any source file

**Purpose**: File-specific instructions for code generation.

**Format**:
```kotlin
/**
 * Copilot Instructions:
 * - This service handles URL shortening
 * - Use reactive patterns
 * - Apply caching strategies
 * - Follow the existing pattern for error handling
 */
@Service
class UrlShortenerService { ... }
```

---

## 🎨 How Copilot Uses These Contexts

Copilot reads context in this priority order:

1. **Inline comments** in the current file (highest priority)
2. **File-level instructions** at the top of the file
3. **Workspace instructions** (`.idea/copilot-instructions.md`)
4. **Repository instructions** (`.github/copilot-instructions.md`)
5. **README files** in the project
6. **Surrounding code** in the current file
7. **Open files** in your editor tabs
8. **Your cursor position** and what you're typing

---

## 🚀 Quick Start: Using Your New Instructions

### For IntelliJ IDEA:

1. **Restart IntelliJ IDEA** (optional, but recommended to load the new files)

2. **Start coding** - Copilot will now use the instructions automatically

3. **Test it** - Open a Kotlin file and type:
   ```kotlin
   // Create a new Spring Boot REST controller for managing items
   ```
   
   Copilot should suggest Kotlin code with:
   - `@RestController` annotation
   - Constructor injection
   - `ResponseEntity` returns
   - RESTful patterns

4. **Use Copilot Chat**:
   - Open Copilot Chat panel (Alt + 4 or View → Tool Windows → GitHub Copilot Chat)
   - Ask: "Generate a service for managing items following the project conventions"
   - Copilot will use your instructions automatically

---

## ✏️ How to Edit These Instructions

### To modify project-wide instructions:
```bash
# Edit the repository-level file
open .github/copilot-instructions.md
# or
nano .github/copilot-instructions.md
```

### To modify workspace instructions:
```bash
# Edit the workspace-level file
open .idea/copilot-instructions.md
# or
nano .idea/copilot-instructions.md
```

### Commit to Git:
```bash
# Add repository-level instructions to Git (recommended)
git add .github/copilot-instructions.md
git commit -m "docs: add GitHub Copilot instructions for project conventions"
git push origin main

# Note: .idea/ is typically gitignored, so workspace instructions 
# are local to your machine only
```

---

## 📊 What I've Set Up For You

| Location | File | Status | In Git? | Purpose |
|----------|------|--------|---------|---------|
| `.github/` | `copilot-instructions.md` | ✅ Created | Yes (recommended) | Project conventions |
| `.idea/` | `copilot-instructions.md` | ✅ Created | No (local) | Quick reference |
| Root | `README.md` | ✅ Exists | Yes | Project overview |
| Root | `RELEASE_GUIDE.md` | ✅ Exists | Yes | Release process |

---

## 🎯 Best Practices

### ✅ DO:
- Keep instructions **concise and specific**
- Use **examples** of good patterns
- Specify **technology stack** clearly
- Update instructions when **patterns change**
- Include **common pitfalls to avoid**
- Commit `.github/copilot-instructions.md` to Git

### ❌ DON'T:
- Write a novel (keep it scannable)
- Include sensitive information
- Contradict yourself across files
- Forget to update when project evolves

---

## 🔍 Verify Copilot is Using Your Instructions

1. Open a Kotlin file
2. Type a comment like: `// Create a service for user management`
3. Press Enter and wait for suggestions
4. Check if suggestions follow your conventions:
   - Uses Kotlin (not Java)
   - Constructor injection
   - Proper annotations
   - Follows your patterns

---

## 📚 Additional Resources

- **GitHub Copilot Docs**: https://docs.github.com/en/copilot
- **IntelliJ Copilot Plugin**: https://plugins.jetbrains.com/plugin/17718-github-copilot
- **Copilot Best Practices**: https://github.blog/developer-skills/github/how-to-use-github-copilot-in-your-ide-tips-tricks-and-best-practices/

---

## 🎉 You're All Set!

GitHub Copilot is now configured with your project's context. It will:
- ✅ Prefer Kotlin over Java
- ✅ Use Spring Boot best practices
- ✅ Follow your MongoDB patterns
- ✅ Generate code matching your style
- ✅ Create tests following your conventions

**Start coding and watch Copilot adapt to your project!** 🚀

