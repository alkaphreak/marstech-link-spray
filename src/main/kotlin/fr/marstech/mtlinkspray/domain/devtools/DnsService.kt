package fr.marstech.mtlinkspray.domain.devtools
import fr.marstech.mtlinkspray.adapter.inbound.web.devtools.DnsLookupResponse
import fr.marstech.mtlinkspray.adapter.inbound.web.devtools.DnsPropagationResponse

interface DnsService {

    fun lookup(name: String, type: String): DnsLookupResponse

    fun reverse(ip: String): DnsLookupResponse

    /** The same query on every [fr.marstech.mtlinkspray.adapter.outbound.dns.PublicDnsResolver], in parallel. */
    fun propagation(name: String, type: String): DnsPropagationResponse
}
