package fr.marstech.mtlinkspray.domain.shared

/** Implemented by the `apikey` domain, so other domains never see API key storage (ADR 0007). */
interface ApiKeyResolver {
    /**
     * Resolves and validates a raw API key (as received in the `X-Api-Key` header).
     * Throws [IllegalAccessException] if the key is blank, malformed, unknown, disabled, or its
     * secret does not match.
     */
    fun resolve(rawApiKey: String?): ApiCaller

    /** Whether an enabled API key owns [namespace]. */
    fun isNamespaceEnabled(namespace: String): Boolean
}
