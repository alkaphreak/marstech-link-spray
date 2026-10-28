package fr.marstech.mtlinkspray.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class DnsLookupRequest(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Pattern(
        // Labels may start with '_' (_dmarc, _acme-challenge), total length at most 253
        regexp = "^(?=.{1,253}\\.?$)([A-Za-z0-9_](?:[A-Za-z0-9_-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}\\.?$",
        message = "Name must be a valid domain name"
    )
    val name: String? = null,

    @field:Pattern(regexp = "(?i)^(A|AAAA|MX|TXT|CNAME|NS)$", message = "Type must be one of A, AAAA, MX, TXT, CNAME, NS")
    val type: String = "A",
)
