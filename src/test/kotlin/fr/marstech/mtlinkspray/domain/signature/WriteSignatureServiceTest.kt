package fr.marstech.mtlinkspray.domain.signature
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.signature.ResourceType.PASTE
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class WriteSignatureServiceTest {
    private val store = mock<WriteSignatureStore>()
    private val service = WriteSignatureService(store)

    private val request = ClientContext(ipAddress = "203.0.113.7", userAgent = "Mozilla/5.0 " + "x".repeat(1000))

    private fun recorded(): WriteSignature =
        argumentCaptor<WriteSignature>().also { verify(store).save(it.capture()) }.firstValue

    @Test
    fun shouldRecordValidFingerprintWithNetworkSide() {
        // When
        service.record(PASTE, "abc123", "AB".repeat(32), request)

        // Then
        val signature = recorded()
        assertEquals(PASTE, signature.resourceType)
        assertEquals("abc123", signature.resourceId)
        assertEquals("ab".repeat(32), signature.fingerprintHash)
        assertEquals("203.0.113.7", signature.ipAddress)
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
        verify(store, never()).save(any<WriteSignature>())
    }

    @Test
    fun shouldNotThrowWhenStorageFails() {
        // Given
        whenever(store.save(any<WriteSignature>())).thenThrow(IllegalStateException("down"))

        // When / Then: the creation itself must go on
        service.record(PASTE, "abc123", null, request)
    }
}
