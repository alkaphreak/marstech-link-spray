package fr.marstech.mtlinkspray.service

import com.maxmind.db.Reader
import fr.marstech.mtlinkspray.dto.IpInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.net.Inet6Address
import java.net.InetAddress

/** DB-IP Lite country and ASN databases (CC BY 4.0), downloaded into the image at build time (MLS-113). */
@Service
class IpServiceImpl(
    @Value("\${mt.link-spray.geoip.dir}") geoipDir: String
) : IpService {

    private val countryReader = open(File(geoipDir, COUNTRY_DB))
    private val asnReader = open(File(geoipDir, ASN_DB))

    override fun lookup(ip: String): IpInfoResponse {
        val address = InetAddress.ofLiteral(ip)
        val country = countryReader?.get(address, Map::class.java)?.get("country") as? Map<*, *>
        val asn = asnReader?.get(address, Map::class.java)
        val asnNumber = (asn?.get("autonomous_system_number") as? Number)?.toLong()
        return IpInfoResponse(
            ip = address.hostAddress,
            version = if (address is Inet6Address) 6 else 4,
            countryCode = country?.get("iso_code") as? String,
            countryName = (country?.get("names") as? Map<*, *>)?.get("en") as? String,
            asn = asnNumber,
            organization = asn?.get("autonomous_system_organization") as? String,
            datacenter = asnNumber in DATACENTER_ASNS,
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
        private val log = LoggerFactory.getLogger(IpServiceImpl::class.java)

        const val COUNTRY_DB = "dbip-country-lite.mmdb"
        const val ASN_DB = "dbip-asn-lite.mmdb"

        // ponytail: fixed list of major hosting / VPN-exit ASNs, a hint only; a maintained list if it matters
        val DATACENTER_ASNS = setOf(
            16509L, 14618L, // Amazon AWS
            15169L, 396982L, // Google
            8075L, // Microsoft Azure
            13335L, // Cloudflare
            14061L, // DigitalOcean
            16276L, // OVH
            24940L, // Hetzner
            63949L, // Akamai Linode
            20473L, // Vultr
            31898L, // Oracle Cloud
            12876L, // Scaleway
            51167L, // Contabo
            60781L, // Leaseweb
            9009L, // M247
            60068L, // Datacamp (CDN77)
        )
    }
}
