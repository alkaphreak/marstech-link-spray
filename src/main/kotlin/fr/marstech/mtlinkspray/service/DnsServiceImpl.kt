package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.DnsPropagationResponse
import fr.marstech.mtlinkspray.enums.PublicDnsResolver
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executors
import javax.naming.NamingException

@Service
class DnsServiceImpl(private val dnsClient: DnsClient) : DnsService {

    override fun lookup(name: String, type: String): DnsLookupResponse =
        query(PublicDnsResolver.CLOUDFLARE, name, type.uppercase())

    override fun reverse(ip: String): DnsLookupResponse =
        query(PublicDnsResolver.CLOUDFLARE, ptrName(ip), "PTR")

    override fun propagation(name: String, type: String): DnsPropagationResponse {
        val recordType = type.uppercase()
        val results = Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            PublicDnsResolver.entries
                .map { executor.submit<DnsLookupResponse> { query(it, name, recordType) } }
                .map { it.get() }
        }
        val answers = results.filter { it.error == null }.map { it.records.toSet() }.distinct()
        return DnsPropagationResponse(name, recordType, results, consistent = answers.size <= 1)
    }

    private fun query(resolver: PublicDnsResolver, name: String, type: String): DnsLookupResponse {
        val start = System.nanoTime()
        val (records, error) = try {
            dnsClient.query(resolver.address, name, type) to null
        } catch (e: NamingException) {
            emptyList<String>() to (e.explanation ?: e.javaClass.simpleName)
        }
        return DnsLookupResponse(name, type, resolver.label, records, (System.nanoTime() - start) / 1_000_000, error)
    }

    companion object {

        /** in-addr.arpa / ip6.arpa name of an IP literal (validated upstream, so no lookup happens here). */
        fun ptrName(ip: String): String {
            val bytes = try {
                InetAddress.getByName(ip).address
            } catch (e: UnknownHostException) {
                throw IllegalArgumentException("Invalid IP address: $ip", e)
            }
            return if (bytes.size == 4) {
                bytes.reversed().joinToString(".") { (it.toInt() and 0xff).toString() } + ".in-addr.arpa"
            } else {
                bytes.flatMap { listOf((it.toInt() shr 4) and 0xf, it.toInt() and 0xf) }
                    .reversed().joinToString(".") { it.toString(16) } + ".ip6.arpa"
            }
        }
    }
}
