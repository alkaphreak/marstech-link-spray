package fr.marstech.mtlinkspray.dto

import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.validation.constraints.Pattern

/** No IP means the caller's own address. */
data class IpLookupRequest(
    @field:Pattern(regexp = NetworkUtils.IP_LITERAL, message = "IP must be an IPv4 or IPv6 address")
    val ip: String? = null,
)
