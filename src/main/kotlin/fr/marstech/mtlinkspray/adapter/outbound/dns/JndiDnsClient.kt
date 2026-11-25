package fr.marstech.mtlinkspray.adapter.outbound.dns
import fr.marstech.mtlinkspray.domain.devtools.DnsClient
import fr.marstech.mtlinkspray.domain.devtools.DnsQueryException
import org.springframework.stereotype.Component
import java.util.*
import javax.naming.Context
import javax.naming.NameNotFoundException
import javax.naming.NamingException
import javax.naming.directory.InitialDirContext

/** JDK DNS provider (module jdk.naming.dns): no extra dependency. */
@Component
class JndiDnsClient : DnsClient {

    override fun query(server: String, name: String, type: String): List<String> {
        // A JNDI name with a scheme (ldap://, rmi://) would leave the DNS provider: only plain DNS names pass
        require(DNS_NAME.matches(name)) { "Invalid DNS name: $name" }
        val env = Hashtable<String, String>().apply {
            put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory")
            put(Context.PROVIDER_URL, "dns://$server")
            put("com.sun.jndi.dns.timeout.initial", TIMEOUT_MS)
            put("com.sun.jndi.dns.timeout.retries", "1")
        }
        try {
            val context = InitialDirContext(env)
            try {
                val attribute = context.getAttributes(name, arrayOf(type)).get(type) ?: return emptyList()
                return (0 until attribute.size()).map { attribute.get(it).toString() }
            } finally {
                context.close()
            }
        } catch (e: NamingException) {
            throw e.toDomain()
        }
    }

    companion object {
        internal fun NamingException.toDomain() =
            DnsQueryException(explanation ?: javaClass.simpleName, this is NameNotFoundException, this)

        private const val TIMEOUT_MS = "2000"
        private val DNS_NAME = Regex("^[A-Za-z0-9_.-]{1,253}$")
    }
}
