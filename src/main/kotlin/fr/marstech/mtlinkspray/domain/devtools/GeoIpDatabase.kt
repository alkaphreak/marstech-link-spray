package fr.marstech.mtlinkspray.domain.devtools

import java.net.InetAddress

/** Country and network owner of an address, implemented over the DB-IP Lite databases in `adapter/outbound/geoip`. */
fun interface GeoIpDatabase {

    /** Every field is null when the address is unknown or the databases are absent. */
    fun lookup(address: InetAddress): GeoIpRecord
}

data class GeoIpRecord(
    val countryCode: String? = null,
    val countryName: String? = null,
    val asn: Long? = null,
    val organization: String? = null,
)
