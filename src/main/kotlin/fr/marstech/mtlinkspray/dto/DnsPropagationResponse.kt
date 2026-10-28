package fr.marstech.mtlinkspray.dto

data class DnsPropagationResponse(
    val name: String,
    val type: String,
    val results: List<DnsLookupResponse>,
    /** Every resolver answered, with records or NXDOMAIN; false when one timed out or failed. */
    val complete: Boolean,
    /** Every resolver answered and all answers are the same. */
    val consistent: Boolean,
)
