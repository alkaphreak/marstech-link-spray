---
status: accepted
date: 2026-10-27
ticket: https://marstech.myjetbrains.com/youtrack/issue/MLS-137
---

# Version the REST API by content negotiation, with Spring 7 native support

## Context and Problem Statement

No endpoint under `/api` is versioned, and clients (agents, the `sol4-api` skill) call paths such as
`/api/url-shortener/shorten` directly. The API needs a way to ship a breaking change without breaking
those clients. How should a client choose the version it gets?

## Considered Options

- Content negotiation: a `version` parameter on the media type (`Accept: application/json;version=1`)
- URI path prefix (`/api/v1/...`)
- Request header (`X-API-Version`) or query parameter (`?version=1`)

## Decision Outcome

Chosen option: "Content negotiation", because URLs stay unchanged: every current client keeps working
and is served version 1 by default, and Spring Framework 7 supports it natively through configuration.

Setup, in `application.properties`:

```properties
spring.mvc.apiversion.supported=1.0
spring.mvc.apiversion.default=1.0
spring.mvc.apiversion.use.media-type-parameter[application/json]=version
```

- A request without a version is served version 1.0.
- An unsupported or malformed version returns a 400 (`InvalidApiVersionException`, mapped in
  `GlobalRestExceptionHandler`), not a 406.
- Thymeleaf pages (`text/html`) are not affected.
- A breaking change adds a method next to the version 1 one, in the same controller, with no
  `v1`/`v2` packages:

```kotlin
@GetMapping("/shorten")                 // version 1, implicit
fun shorten(@RequestParam url: String): String = ...

@GetMapping("/shorten", version = "2")  // Accept: application/json;version=2
fun shortenV2(@RequestParam url: String): ShortenResponseV2 = ...
```

Deferred until the first version 2 exists, since nothing needs them before:

- `Vary: Accept` on `/api` responses, so caches (Cloudflare in front of sol4.space) keep the
  representations apart; Spring does not add it.
- Deprecation headers (`Deprecation`, `Sunset`, `Link`) through `StandardApiVersionDeprecationHandler`.
- `2.0` in `spring.mvc.apiversion.supported`.

### Consequences

- Good: no client migration, no URL aliases, no duplicated controllers or packages.
- Good: three properties instead of a custom interceptor and path constants.
- Bad: the version is not visible in URLs or access logs; log it explicitly if needed.
- Bad: a shared cache must vary on `Accept` once a second version exists.
- Bad: OpenAPI Generator does not handle several media types for one path (issue #11000); no impact
  today, MLS has no published spec.

## Pros and Cons of the Options

### URI path prefix

- Good: explicit, easy to route and cache by URL.
- Bad: breaks every current call to `/api/...`, or needs aliases for all of them.
- Bad: tends to duplicate controllers and DTO packages per version.

### Request header or query parameter

- Good: URLs stay unchanged, like content negotiation.
- Bad: a custom header or parameter instead of the standard `Accept` negotiation; the version is not
  part of the representation's media type.

## More Information

Decision taken on 2026-10-24 (MLS-137 comment). Sources: Spring Framework 7 reference, API Versioning
(Web MVC); MDN, Content negotiation; Baeldung, API Versioning in Spring, section 3.3.
