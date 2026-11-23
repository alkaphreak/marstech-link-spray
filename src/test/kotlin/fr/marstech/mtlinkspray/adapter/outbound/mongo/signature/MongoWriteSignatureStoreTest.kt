package fr.marstech.mtlinkspray.adapter.outbound.mongo.signature
import fr.marstech.mtlinkspray.domain.signature.ResourceType.PASTE
import fr.marstech.mtlinkspray.domain.signature.WriteSignature
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexOperations
import kotlin.test.assertEquals

internal class MongoWriteSignatureStoreTest {
    private val repository = mock<WriteSignatureRepository>()
    private val mongoTemplate = mock<MongoTemplate>()
    private val store = MongoWriteSignatureStore(repository, mongoTemplate)

    @Test
    fun shouldMapSignatureBothWays() {
        // Given
        val signature = WriteSignature(resourceType = PASTE, resourceId = "abc123", fingerprintHash = "ab".repeat(32), ipAddress = "203.0.113.7", userAgent = "ua")

        // When
        store.save(signature)

        // Then
        val entity = argumentCaptor<WriteSignatureEntity>().also { verify(repository).save(it.capture()) }.firstValue
        assertEquals(signature, entity.toDomain())
    }

    @Test
    fun shouldNotTouchIndexOnSave() {
        // When: MongoDB index not created yet
        store.save(WriteSignature(resourceType = PASTE, resourceId = "abc123", fingerprintHash = null, ipAddress = null, userAgent = null))

        // Then: the request path never waits on createIndex
        verify(mongoTemplate, never()).indexOps(WriteSignatureEntity::class.java)
    }

    @Test
    fun shouldCreateTtlIndexOfTwelveMonths() {
        // Given
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(WriteSignatureEntity::class.java)).thenReturn(indexOps)

        // When
        store.ensureRetentionIndex()

        // Then
        val index = argumentCaptor<IndexDefinition>().also { verify(indexOps).createIndex(it.capture()) }.firstValue
        assertEquals(365L * 24 * 3600, index.indexOptions.getLong("expireAfterSeconds"))
        assertEquals(1, index.indexKeys.getInteger("createdAt"))
    }

    @Test
    fun shouldRetryTtlIndexUntilCreatedWithCappedBackoff() {
        // Given: MongoDB down for 7 attempts, then up
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(WriteSignatureEntity::class.java)).thenReturn(indexOps)
        var calls = 0
        whenever(indexOps.createIndex(any<IndexDefinition>())).thenAnswer { if (++calls <= 7) throw IllegalStateException("down") else "createdAt_1" }
        val sleeps = mutableListOf<Long>()

        // When
        store.createRetentionIndexWithRetry { sleeps += it.seconds }

        // Then
        assertEquals(8, calls)
        assertEquals(listOf(1L, 2L, 4L, 8L, 16L, 32L, 60L), sleeps)
    }

    @Test
    fun shouldStopTtlIndexRetryWhenInterruptedDuringSleep() {
        // Given: MongoDB down
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(WriteSignatureEntity::class.java)).thenReturn(indexOps)
        whenever(indexOps.createIndex(any<IndexDefinition>())).thenThrow(IllegalStateException("down"))

        // When: shutdown interrupts the sleep
        store.createRetentionIndexWithRetry { throw InterruptedException() }

        // Then: one attempt, loop left
        verify(indexOps, times(1)).createIndex(any<IndexDefinition>())
    }

    @Test
    fun shouldNotRetryTtlIndexOnceStopped() {
        // Given: context closed
        store.stopRetentionIndexRetry()

        // When
        store.createRetentionIndexWithRetry { error("must not sleep") }

        // Then
        verify(mongoTemplate, never()).indexOps(WriteSignatureEntity::class.java)
    }

    @Test
    fun shouldStartTtlIndexCreationOnApplicationReady() {
        // Given
        val indexOps = mock<IndexOperations>()
        val created = java.util.concurrent.CountDownLatch(1)
        whenever(mongoTemplate.indexOps(WriteSignatureEntity::class.java)).thenReturn(indexOps)
        whenever(indexOps.createIndex(any<IndexDefinition>())).thenAnswer { created.countDown(); "createdAt_1" }

        // When
        store.onApplicationReady()

        // Then
        assertEquals(true, created.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }
}
