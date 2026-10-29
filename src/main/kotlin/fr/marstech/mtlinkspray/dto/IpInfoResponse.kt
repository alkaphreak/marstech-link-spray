package fr.marstech.mtlinkspray.dto

/** Country and ASN fields are null when the address is private or the GeoIP databases are absent. */
data class IpInfoResponse(
    val ip: String,
    val version: Int,
    val countryCode: String?,
    val countryName: String?,
    val asn: Long?,
    val organization: String?,
    val datacenter: Boolean,
)
