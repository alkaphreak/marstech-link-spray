package fr.marstech.mtlinkspray.service

import org.springframework.stereotype.Component
import java.util.*
import javax.naming.Context
import javax.naming.directory.InitialDirContext

fun interface DnsClient {

    /**
     * Query one record type of [name] on the DNS server at [server] (host, optionally host:port).
     *
     * @throws javax.naming.NamingException when the name does not exist or the server does not answer
     */
    fun query(server: String, name: String, type: String): List<String>
}

/** JDK DNS provider (module jdk.naming.dns): no extra dependency. */
@Component
class JndiDnsClient : DnsClient {

    override fun query(server: String, name: String, type: String): List<String> {
        val env = Hashtable<String, String>().apply {
            put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory")
            put(Context.PROVIDER_URL, "dns://$server")
            put("com.sun.jndi.dns.timeout.initial", TIMEOUT_MS)
            put("com.sun.jndi.dns.timeout.retries", "1")
        }
        val context = InitialDirContext(env)
        try {
            val attribute = context.getAttributes(name, arrayOf(type)).get(type) ?: return emptyList()
            return (0 until attribute.size()).map { attribute.get(it).toString() }
        } finally {
            context.close()
        }
    }

    companion object {
        private const val TIMEOUT_MS = "2000"
    }
}
