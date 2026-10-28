package fr.marstech.mtlinkspray.enums

/**
 * The only resolvers the DNS tools query: a caller never picks the server, so the API cannot be used to
 * probe hosts inside the network (MLS-104).
 */
enum class PublicDnsResolver(val label: String, val address: String) {
    CLOUDFLARE("Cloudflare", "1.1.1.1"),
    GOOGLE("Google", "8.8.8.8"),
    QUAD9("Quad9", "9.9.9.9"),
    OPENDNS("OpenDNS", "208.67.222.222"),
}
