# GitHub Copilot Instructions for MarsTech Link Spray

## Project Overview
MarsTech Link Spray is a URL shortener and link management service built with:
- **Language**: Kotlin 2.3, Java 21 (Eclipse Temurin)
- **Framework**: Spring Boot 3.5+, Spring WebMVC
- **Database**: MongoDB
- **Build Tool**: Maven
- **Java Version**: 21 (Temurin)
- **Testing**: JUnit 5, MockMvc, Mockito-Kotlin (`org.mockito.kotlin`)

## Coding Standards & Preferences

### Language Preference
- **Always prefer Kotlin to Java** for new code
- Use Kotlin idiomatic patterns (data classes, extension functions, null safety)
- Convert Java code to Kotlin when refactoring or extending

### Code Style
- Use **functional programming style** where appropriate
- Prefer **immutability** (use `val` over `var`)
- Use **data classes** for DTOs and entities
- Apply **null safety** (`?`, `!!`, `?.let`)
- Use **sealed classes** for state/result modeling
- Prefer **extension functions** over utility classes

### Spring Boot Conventions
- Use **constructor injection** (not field injection)
- Prefer **functional endpoints** where appropriate
- Use **@RestController** for API controllers
- Apply **@Validated** for validation
- Use **ResponseEntity** for API responses
- Follow REST conventions (proper HTTP methods and status codes)

### MongoDB & Data Layer
- Use **Spring Data MongoDB** repositories
- Define indexes with `@Indexed` annotations
- Use `@Document` for collections
- Prefer **reactive patterns** where beneficial
- Use `@Query` for complex queries

### Testing Guidelines
- Write **unit tests** for services and utilities
- Write **integration tests** with `@SpringBootTest` for controllers
- Use **MockMvc** for REST API testing
- Mock dependencies with **Mockito-Kotlin** (`org.mockito.kotlin`) — use `@MockitoBean`, `@Mock`, `whenever()`
- Test profile: `test` (uses local MongoDB on port 27018 — start with `docker-compose.test.yml`)
- Aim for **high code coverage**
- Use **Given-When-Then** structure for test methods
- **ALWAYS use camelCase** method names (e.g., `shouldReturnUserWhenUserExists`)
- **NEVER use backticks** in test method names (e.g., ❌ `fun \`should return user\`()`)
- Add `@DisplayName` for human-readable descriptions (optional)

### API Design
- Follow **RESTful conventions**
- Use proper HTTP status codes (200, 201, 400, 404, 409, 500)
- Return structured error responses with `ErrorResponse` DTO
- Use **DTOs** for request/response (separate from entities)
- Apply **validation annotations** (`@NotBlank`, `@Valid`, etc.)
- Document endpoints with meaningful names and descriptions

### Error Handling
- Use custom exceptions (`UrlNotFoundException`, `InvalidUrlException`, etc.)
- Implement `@ControllerAdvice` for global error handling
- Return consistent error response format
- Log errors appropriately with context

### Security
- Validate and sanitize all user inputs
- Apply **rate limiting** where appropriate
- Use **CORS configuration** for web endpoints
- Sanitize URLs to prevent XSS/injection attacks

### Documentation
- Write **meaningful commit messages** (conventional commits format) — when a YouTrack ticket is available, append the ticket ID and full URL on a trailing line:
  ```
  feat(shortener): add expiry support

  MLS-142 https://marstech.myjetbrains.com/youtrack/issue/MLS-142
  ```
- Add **KDoc comments** for public APIs
- Include **README** updates for new features
- Document environment variables and configuration

### Release & Versioning
- Follow **semantic versioning** (MAJOR.MINOR.PATCH)
- Use **JReleaser** for automated releases
- Update **CHANGELOG.md** for significant changes
- Version format: `X.Y.Z` (e.g., `0.1.0`)

## Project Structure
```
src/main/kotlin/fr/marstech/mtlinkspray/
├── conf/           # Spring configuration classes
├── controller/     # REST controllers
│   ├── api/        # API endpoints (@RestController)
│   ├── view/       # Thymeleaf view controllers
│   └── commons/    # Shared controller utilities
├── dto/            # Request/response DTOs (never return entities from controllers)
├── entity/         # MongoDB @Document classes
├── enums/          # Enumerations
├── exception/      # Custom exceptions + @ControllerAdvice handlers
├── objects/        # Constants / singleton objects
├── repository/     # Spring Data MongoDB repositories
├── service/        # Service interfaces + Impl pairs
├── utils/          # Pure utility functions
└── validation/     # Custom JSR-380 validators
```

## Common Patterns in This Project

### Service Layer Pattern
```kotlin
@Service
class MyService(
    private val repository: MyRepository,
    private val anotherService: AnotherService
) {
    fun performAction(param: String): Result {
        // Business logic here
    }
}
```

### Controller Pattern
```kotlin
@RestController
@RequestMapping("/api/v1/resource")
class MyController(
    private val service: MyService
) {
    @GetMapping("/{id}")
    fun getResource(@PathVariable id: String): ResponseEntity<ResourceDto> {
        // Delegate to service
    }
}
```

### Repository Pattern
```kotlin
@Repository
interface MyRepository : MongoRepository<MyEntity, String> {
    fun findByCustomField(field: String): Optional<MyEntity>
}
```

## When Suggesting Code

1. **Check existing patterns** in the codebase first
2. **Match the project's style** and conventions
3. **Use Kotlin features** (not Java-style Kotlin)
4. **Include proper error handling** and validation
5. **Add tests** when suggesting new features (use Given-When-Then structure)
6. **Follow RESTful** conventions for APIs
7. **Consider MongoDB indexing** for queries
8. **Apply Spring Boot best practices**

## Environment & Configuration

- **Dev profile**: Uses MongoDB on `localhost:27017`
- **Test profile**: Uses MongoDB on `localhost:27018`
- **Prod profile**: Uses environment variable `MONGODB_URI_LINK_SPRAY`
- **Port**: Default 8080 (configurable)

## Important Files
- `pom.xml` - Maven dependencies and build config
- `application.properties` - Spring Boot configuration
- `jreleaser.yml` - Release automation config
- `RELEASE_GUIDE.md` - Release process documentation

## Documentation & LLM-Generated Files

### LLM Documentation Location
- **All LLM-generated specifications, analyses, and summaries** must be saved in `.local/llm/`
- **DO NOT** save specification files in the project root directory

### Naming Conventions for LLM Docs
- Specifications: `{ISSUE-ID}-specification.md` (e.g., `MLS-129-specification.md`)
- Implementation summaries: `{ISSUE-ID}-{PHASE}-COMPLETE.md` (e.g., `MLS-129-PHASE2-COMPLETE.md`)
- Progress tracking: `{ISSUE-ID}-progress.md`
- Session summaries: `{ISSUE-ID}-session{N}-{topic}.md`
- Quick guides: `{ISSUE-ID}-quick-guide.md`

### What Goes in `.local/llm/`
- ✅ Issue analysis and specifications
- ✅ Implementation plans and summaries
- ✅ Progress tracking documents
- ✅ Session notes and summaries
- ✅ Quick reference guides
- ✅ Test result summaries

### What Stays in Project Root
- ❌ NOT specification files
- ❌ NOT LLM analysis documents
- ✅ Official project documentation (README, CHANGELOG, etc.)
- ✅ Release guides and contribution docs

See `.local/llm/README.md` for complete documentation conventions.

## Avoid
- ❌ Java-style code in Kotlin files
- ❌ Field injection (`@Autowired` on fields)
- ❌ Mutable state without good reason
- ❌ Unsafe null handling (`!!` without justification)
- ❌ God classes or services
- ❌ Hardcoded values (use configuration)
- ❌ Missing validation on public APIs
- ❌ Returning raw entities from controllers

## Prefer
- ✅ Kotlin idiomatic code
- ✅ Constructor injection
- ✅ Immutable data structures
- ✅ Extension functions
- ✅ Sealed classes for state
- ✅ Data classes for DTOs
- ✅ Functional programming patterns
- ✅ Comprehensive tests

## Test Naming Convention Examples

### ✅ Correct (camelCase)
```kotlin
@Test
fun shouldReturnUserWhenUserExists() {
    // Given-When-Then test implementation
}

@Test
fun shouldThrowExceptionWhenUserNotFound() {
    // Given-When-Then test implementation
}

@Test
fun getSprayPageShouldReturnCorrectView() {
    // Given-When-Then test implementation
}

@Test
fun postViewShouldAddInputLinkAndShortenedLinkToModel() {
    // Given-When-Then test implementation
}
```

### ❌ Incorrect (backticks - DO NOT USE)
```text
@Test
fun `should return user when user exists`() { }  // ❌ NO BACKTICKS

@Test
fun `GET spray page returns correct view`() { }  // ❌ NO BACKTICKS

@Test
fun `POST spray returns correct view with model`() { }  // ❌ NO BACKTICKS
```

### Optional: @DisplayName for readability
```kotlin
@Test
@DisplayName("Should return user when user exists")
fun shouldReturnUserWhenUserExists() {
    // Given-When-Then test implementation
}
```

