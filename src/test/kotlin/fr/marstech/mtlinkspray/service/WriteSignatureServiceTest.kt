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
    fun shouldRetryTtlIndexOnRecordUntilCreated() {
        // Given: MongoDB down at startup, back for the next writes
        val indexOps = mock<IndexOperations>()
        whenever(mongoTemplate.indexOps(WriteSignatureEntity::class.java)).thenReturn(indexOps)
        whenever(indexOps.createIndex(any<IndexDefinition>()))
            .thenThrow(IllegalStateException("down"))
            .thenReturn("createdAt_1")
        service.ensureRetentionIndex()

        // When
        service.record(PASTE, "abc123", null, request)
        service.record(PASTE, "def456", null, request)

        // Then: one failed startup attempt, one retry on the first write, none after
        verify(indexOps, times(2)).createIndex(any<IndexDefinition>())
    }
}
