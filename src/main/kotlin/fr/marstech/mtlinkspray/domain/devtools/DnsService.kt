package fr.marstech.mtlinkspray.domain.devtools

interface DnsService {

    fun lookup(name: String, type: String): DnsLookupResult

    fun reverse(ip: String): DnsLookupResult

    /** The same query on every [fr.marstech.mtlinkspray.domain.devtools.PublicDnsResolver], in parallel. */
    fun propagation(name: String, type: String): DnsPropagationResult
}
