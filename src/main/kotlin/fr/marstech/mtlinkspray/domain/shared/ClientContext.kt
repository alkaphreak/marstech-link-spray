package fr.marstech.mtlinkspray.domain.shared

/**
 * The calling client as the domain sees it, built by the web adapter in place of
 * `HttpServletRequest` (ADR 0007). Further fields (base URL) join with their first user.
 */
data class ClientContext(
    val ipAddress: String?,
    val userAgent: String? = null,
)
