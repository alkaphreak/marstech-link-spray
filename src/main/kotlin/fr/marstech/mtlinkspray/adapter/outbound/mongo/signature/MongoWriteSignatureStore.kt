package fr.marstech.mtlinkspray.adapter.outbound.mongo.signature

import fr.marstech.mtlinkspray.domain.signature.WriteSignature
import fr.marstech.mtlinkspray.domain.signature.WriteSignatureStore
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.stereotype.Component
import java.time.Duration

/** Write signatures in MongoDB, expired by a TTL index of [WriteSignature.RETENTION] on `createdAt`. */
@Component
class MongoWriteSignatureStore(
    private val repository: WriteSignatureRepository,
    private val mongoTemplate: MongoTemplate,
) : WriteSignatureStore {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun save(signature: WriteSignature) {
        repository.save(WriteSignatureEntity.of(signature))
    }

    override fun findLatestByResourceIdContaining(query: String): List<WriteSignature> =
        repository.findTop50ByResourceIdContainingOrderByCreatedAtDesc(query).map { it.toDomain() }

    // Auto index creation is off in this app, so the TTL index is created explicitly (idempotent).
    // Off the startup thread: an unreachable MongoDB (test profile) must neither block nor fail startup.
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        retentionIndexThread = Thread.ofVirtual().name("write-signature-ttl-index").start { createRetentionIndexWithRetry() }
    }

    private var retentionIndexThread: Thread? = null

    // Own flag, not the interrupt status: a driver wait can clear the interrupt before throwing a RuntimeException
    @Volatile
    private var retentionIndexRetryStopped = false

    // A closed context (devtools restart, evicted test context) must not keep retrying against a closed MongoClient
    @PreDestroy
    fun stopRetentionIndexRetry() {
        retentionIndexRetryStopped = true
        retentionIndexThread?.interrupt()
    }

    // Retries in the background until MongoDB accepts the index, backing off from 1s to 60s, so a MongoDB that was
    // down at startup does not leave signatures stored without expiry, and no request ever waits on createIndex.
    // Signatures written meanwhile still expire: a TTL index applies to documents that predate it.
    // ponytail: a permanent failure (index option conflict, no createIndex right) retries every 60s forever;
    // stop on MongoCommandException code 85/86 if that ever shows in the logs.
    fun createRetentionIndexWithRetry(sleep: (Duration) -> Unit = Thread::sleep) {
        var delay = Duration.ofSeconds(1)
        var firstAttempt = true
        while (!retentionIndexRetryStopped && !ensureRetentionIndex(firstAttempt)) {
            firstAttempt = false
            try {
                sleep(delay)
            } catch (_: InterruptedException) {
                return
            }
            delay = minOf(delay.multipliedBy(2), Duration.ofSeconds(60))
        }
    }

    fun ensureRetentionIndex(firstAttempt: Boolean = true): Boolean =
        try {
            mongoTemplate.indexOps(WriteSignatureEntity::class.java)
                .createIndex(Index().on("createdAt", ASC).expire(WriteSignature.RETENTION))
            true
        } catch (e: RuntimeException) {
            if (firstAttempt) log.error("Write signature TTL index not created, retrying: signatures will not expire meanwhile", e)
            else log.warn("Write signature TTL index still not created: {}", e.toString())
            false
        }
}
