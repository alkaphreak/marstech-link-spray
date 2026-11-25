package fr.marstech.mtlinkspray.domain.devtools

/** A query that got no records: [nxdomain] when the name does not exist, otherwise no usable answer. */
class DnsQueryException(val reason: String, val nxdomain: Boolean, cause: Throwable? = null) : RuntimeException(reason, cause)

/** DNS queries against one server, implemented by the JNDI adapter in `adapter/outbound/dns`. */
fun interface DnsClient {

    /**
     * Query one record type of [name] on the DNS server at [server] (host, optionally host:port).
     *
     * @throws DnsQueryException when the name does not exist or the server does not answer
     */
    fun query(server: String, name: String, type: String): List<String>
}
