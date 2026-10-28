package fr.marstech.mtlinkspray.dto

data class DnsPropagationResponse(
    val name: String,
    val type: String,
    val results: List<DnsLookupResponse>,
    /** True when every resolver that answered returned the same records. */
    val consistent: Boolean,
)
