package fr.marstech.mtlinkspray.domain.devtools
data class DnsPropagationResult(
    val name: String,
    val type: String,
    val results: List<DnsLookupResult>,
    /** Every resolver answered, with records or NXDOMAIN; false when one timed out or failed. */
    val complete: Boolean,
    /** Every resolver answered and all answers are the same. */
    val consistent: Boolean,
)
