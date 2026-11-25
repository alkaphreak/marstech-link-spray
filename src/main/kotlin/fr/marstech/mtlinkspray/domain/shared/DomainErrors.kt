package fr.marstech.mtlinkspray.domain.shared

// Domain errors (ADR 0007), mapped to HTTP by GlobalRestExceptionHandler. A missing resource stays
// NoSuchElementException (404) and invalid input IllegalArgumentException (400).

/** A valid caller that does not own the resource (403, MLS-279). */
class ForbiddenException(message: String) : RuntimeException(message)

/** The resource changed or is taken (409): a stale version, a slug in use. */
class ConflictException(message: String) : RuntimeException(message)

/** A shared engine is busy or unavailable (503, MLS-252). */
class BusyException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/** A client spent its quota (429, ADR 0006); [retryAfterSeconds] goes to the Retry-After header. */
class QuotaExceededException(message: String, val retryAfterSeconds: Long) : RuntimeException(message)
