package fr.marstech.mtlinkspray.dto

data class DnsLookupResponse(
    val name: String,
    val type: String,
    val resolver: String,
    val records: List<String>,
    val timeMs: Long,
    val error: String? = null,
    /** The resolver answered that the name does not exist: a negative answer, not a failure. */
    val nxdomain: Boolean = false,
)
