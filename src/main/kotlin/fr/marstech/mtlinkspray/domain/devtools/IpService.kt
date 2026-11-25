package fr.marstech.mtlinkspray.domain.devtools
import org.springframework.stereotype.Service
import java.net.Inet6Address
import java.net.InetAddress

@Service
class IpService(private val geoIp: GeoIpDatabase) {

    /** [ip] must be an IP literal: it is never resolved as a host name. */
    fun lookup(ip: String): IpInfo {
        val address = InetAddress.ofLiteral(ip)
        val record = geoIp.lookup(address)
        return IpInfo(
            ip = address.hostAddress,
            version = if (address is Inet6Address) 6 else 4,
            countryCode = record.countryCode,
            countryName = record.countryName,
            asn = record.asn,
            organization = record.organization,
            datacenter = record.asn in DATACENTER_ASNS,
        )
    }

    companion object {
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
