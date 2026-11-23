package fr.marstech.mtlinkspray.domain.shortener
class UrlShorteningException(
    message: String?,
    cause: Throwable?
) : RuntimeException(message, cause)
