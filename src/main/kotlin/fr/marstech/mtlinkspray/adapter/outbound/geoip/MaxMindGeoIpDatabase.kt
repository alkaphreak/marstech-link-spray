package fr.marstech.mtlinkspray.adapter.outbound.geoip

import com.maxmind.db.Reader
import fr.marstech.mtlinkspray.domain.devtools.GeoIpDatabase
import fr.marstech.mtlinkspray.domain.devtools.GeoIpRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.net.InetAddress

/** DB-IP Lite country and ASN databases (CC BY 4.0), downloaded into the image at build time (MLS-113). */
@Component
class MaxMindGeoIpDatabase(
    @Value("\${mt.link-spray.geoip.dir}") geoipDir: String
) : GeoIpDatabase {

    private val countryReader = open(File(geoipDir, COUNTRY_DB))
    private val asnReader = open(File(geoipDir, ASN_DB))

    override fun lookup(address: InetAddress): GeoIpRecord {
        val country = countryReader?.get(address, Map::class.java)?.get("country") as? Map<*, *>
        val asn = asnReader?.get(address, Map::class.java)
        return GeoIpRecord(
            countryCode = country?.get("iso_code") as? String,
            countryName = (country?.get("names") as? Map<*, *>)?.get("en") as? String,
            asn = (asn?.get("autonomous_system_number") as? Number)?.toLong(),
            organization = asn?.get("autonomous_system_organization") as? String,
        )
    }

    private fun open(file: File): Reader? =
        if (file.isFile) {
            Reader(file)
        } else {
            log.warn("GeoIP database {} not found: lookups return no country or ASN", file.path)
            null
        }

    companion object {
        private val log = LoggerFactory.getLogger(MaxMindGeoIpDatabase::class.java)

        const val COUNTRY_DB = "dbip-country-lite.mmdb"
        const val ASN_DB = "dbip-asn-lite.mmdb"
    }
}
