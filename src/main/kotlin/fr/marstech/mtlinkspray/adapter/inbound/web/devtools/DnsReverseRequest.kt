package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class DnsReverseRequest(
    @field:NotBlank(message = "IP cannot be blank")
    @field:Pattern(regexp = NetworkUtils.IP_LITERAL, message = "IP must be an IPv4 or IPv6 address")
    val ip: String? = null,
)
