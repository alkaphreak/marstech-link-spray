# GitHub Copilot Instructions for MarsTech Link Spray

## Project Overview
MarsTech Link Spray is a URL shortener and link management service built with:
- **Language**: Kotlin (Spring Boot)
- **Framework**: Spring Boot 3.5.6, Spring WebMVC
- **Database**: MongoDB
- **Build Tool**: Maven
- **Java Version**: 21 (Temurin)
- **Testing**: JUnit 5, MockMvc, Mockito

## Coding Standards & Preferences

### Language Preference
- **Always prefer Kotlin over Java** for new code
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
- Mock dependencies with **Mockito** (`@MockBean`, `@Mock`)
- Test profile: `test` (uses port 27018 for MongoDB)
- Aim for **high code coverage**

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
- Write **meaningful commit messages** (conventional commits format)
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
├── conf/           # Configuration classes
├── controller/     # REST controllers
│   ├── api/       # API endpoints
│   └── view/      # View controllers
├── dto/           # Data Transfer Objects
├── exception/     # Custom exceptions
├── model/         # Domain models/entities
├── repository/    # MongoDB repositories
├── service/       # Business logic
│   └── impl/     # Service implementations
└── util/          # Utility classes
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
5. **Add tests** when suggesting new features
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

