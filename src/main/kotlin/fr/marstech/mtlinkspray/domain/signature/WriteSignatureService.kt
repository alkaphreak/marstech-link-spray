package fr.marstech.mtlinkspray.domain.signature
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Records a signature for each resource created through the web UI (MLS-254 phase B), on the
 * legitimate-interest basis stated on /privacy. Recording never blocks the creation itself.
 */
@Service
class WriteSignatureService(
    private val store: WriteSignatureStore,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun record(type: ResourceType, resourceId: String?, fingerprintHash: String?, client: ClientContext) {
        if (resourceId.isNullOrBlank()) return
        try {
            store.save(
                WriteSignature(
                    resourceType = type,
                    resourceId = resourceId,
                    fingerprintHash = fingerprintHash?.lowercase()?.takeIf(SHA256_HEX::matches),
                    ipAddress = client.ipAddress,
                    userAgent = client.userAgent?.take(MAX_USER_AGENT_LENGTH),
                )
            )
        } catch (e: RuntimeException) {
            log.warn("Write signature not recorded for {} {}", type, resourceId, e)
        }
    }

    fun findByResourceId(query: String): List<WriteSignature> = store.findLatestByResourceIdContaining(query.trim())

    companion object {
        private val SHA256_HEX = Regex("[0-9a-f]{64}")
        private const val MAX_USER_AGENT_LENGTH = 512
    }
}
