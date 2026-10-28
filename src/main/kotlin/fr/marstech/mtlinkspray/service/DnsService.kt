package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.DnsPropagationResponse

interface DnsService {

    fun lookup(name: String, type: String): DnsLookupResponse

    fun reverse(ip: String): DnsLookupResponse

    /** The same query on every [fr.marstech.mtlinkspray.enums.PublicDnsResolver], in parallel. */
    fun propagation(name: String, type: String): DnsPropagationResponse
}
