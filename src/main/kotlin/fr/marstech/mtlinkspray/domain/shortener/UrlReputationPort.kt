package fr.marstech.mtlinkspray.domain.shortener

/**
 * Tells whether a URL is a known malware URL before it is shortened (MLS-120). Exact listed URLs only:
 * a variant (query string, fragment, another shortener in front) is not caught.
 */
fun interface UrlReputationPort {

    /** True only for a URL the provider lists; false when it is unknown or the provider cannot answer (fail-open). */
    fun isMalicious(url: String): Boolean

    /** True when any of [urls] is listed; an adapter may check them in parallel under one deadline. */
    fun anyMalicious(urls: Collection<String>): Boolean = urls.any(::isMalicious)
}
