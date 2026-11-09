package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.WriteSignatureEntity
import fr.marstech.mtlinkspray.entity.WriteSignatureEntity.ResourceType.PASTE
import fr.marstech.mtlinkspray.repository.WriteSignatureRepository
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
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class WriteSignatureServiceTest {
    private val repository = mock<WriteSignatureRepository>()
    private val mongoTemplate = mock<MongoTemplate>()
    private val service = WriteSignatureService(repository, mongoTemplate)

    private val request = MockHttpServletRequest().apply {
        remoteAddr = "203.0.113.7"
        addHeader("User-Agent", "Mozilla/5.0 " + "x".repeat(1000))
    }

    private fun recorded(): WriteSignatureEntity =
        argumentCaptor<WriteSignatureEntity>().also { verify(repository).save(it.capture()) }.firstValue

    @Test
    fun shouldRecordValidFingerprintWithNetworkSide() {
        // When
        service.record(PASTE, "abc123", "AB".repeat(32), request)

        // Then
        val signature = recorded()
        assertEquals(PASTE, signature.resourceType)
        assertEquals("abc123", signature.resourceId)
        assertEquals("ab".repeat(32), signature.fingerprintHash)
        assertEquals(512, signature.userAgent?.length)
    }

    @Test
    fun shouldDropMalformedFingerprint() {
        // When
        service.record(PASTE, "abc123", "<script>alert(1)</script>", request)

        // Then
        assertNull(recorded().fingerprintHash)
    }

    @Test
    fun shouldSkipBlankResourceId() {
        // When
        service.record(PASTE, null, "ab".repeat(32), request)

        // Then
        verify(repository, never()).save(any<WriteSignatureEntity>())
    }

    @Test
    fun shouldNotThrowWhenStorageFails() {
        // Given
        whenever(repository.save(any<WriteSignatureEntity>())).thenThrow(IllegalStateException("down"))

        // When / Then: the creation itself must go on
        service.record(PASTE, "abc123", null, request)
    }

    @Test
    fun shouldCreateTtlIndexOfTwelveMonths() {
        // Given
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(WriteSignatureEntity::class.java)).thenReturn(indexOps)

        // When
        service.ensureRetentionIndex()

        // Then
        val index = argumentCaptor<IndexDefinition>().also { verify(indexOps).createIndex(it.capture()) }.firstValue
        assertEquals(365L * 24 * 3600, index.indexOptions.getLong("expireAfterSeconds"))
        assertEquals(1, index.indexKeys.getInteger("createdAt"))
    }

    @Test
    fun shouldNotTouchIndexOnRecord() {
        // When: MongoDB index not created yet
        service.record(PASTE, "abc123", null, request)

        // Then: the request path never waits on createIndex
        verify(mongoTemplate, never()).indexOps(WriteSignatureEntity::class.java)
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
        service.createRetentionIndexWithRetry { sleeps += it.seconds }

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
        service.createRetentionIndexWithRetry { throw InterruptedException() }

        // Then: one attempt, loop left
        verify(indexOps, times(1)).createIndex(any<IndexDefinition>())
    }

    @Test
    fun shouldNotRetryTtlIndexOnceStopped() {
        // Given: context closed
        service.stopRetentionIndexRetry()

        // When
        service.createRetentionIndexWithRetry { error("must not sleep") }

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
        service.onApplicationReady()

        // Then
        assertEquals(true, created.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }
}
