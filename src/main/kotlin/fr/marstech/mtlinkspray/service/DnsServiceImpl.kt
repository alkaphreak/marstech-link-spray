package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.DnsPropagationResponse
import fr.marstech.mtlinkspray.enums.PublicDnsResolver
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.Executors
import javax.naming.NameNotFoundException
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
        val answers = results.filter { it.error == null || it.nxdomain }.map { if (it.nxdomain) null else it.records.toSet() }
        val complete = answers.size == results.size
        return DnsPropagationResponse(name, recordType, results, complete, consistent = complete && answers.distinct().size == 1)
    }

    private fun query(resolver: PublicDnsResolver, name: String, type: String): DnsLookupResponse {
        val start = System.nanoTime()
        val response = try {
            DnsLookupResponse(name, type, resolver.label, dnsClient.query(resolver.address, name, type), 0)
        } catch (e: NamingException) {
            DnsLookupResponse(
                name, type, resolver.label, emptyList(), 0,
                error = e.explanation ?: e.javaClass.simpleName,
                nxdomain = e is NameNotFoundException,
            )
        }
        return response.copy(timeMs = (System.nanoTime() - start) / 1_000_000)
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
