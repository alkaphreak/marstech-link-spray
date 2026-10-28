package fr.marstech.mtlinkspray.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class DnsReverseRequest(
    @field:NotBlank(message = "IP cannot be blank")
    @field:Pattern(
        // An IP literal only: a host name here would trigger a lookup when parsed
        regexp = "^((25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1?\\d?\\d)$|^[0-9A-Fa-f.]*:[0-9A-Fa-f:.]*$",
        message = "IP must be an IPv4 or IPv6 address"
    )
    val ip: String? = null,
)
