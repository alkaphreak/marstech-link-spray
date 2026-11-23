package fr.marstech.mtlinkspray.domain.shared

/**
 * The calling client as the domain sees it, built by the web adapter in place of
 * `HttpServletRequest` (ADR 0007). Further fields (base URL, user agent) join with their first user.
 */
data class ClientContext(
    val ipAddress: String?,
)
