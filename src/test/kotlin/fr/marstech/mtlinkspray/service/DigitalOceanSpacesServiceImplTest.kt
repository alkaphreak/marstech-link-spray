package fr.marstech.mtlinkspray.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.ByteArrayInputStream

class DigitalOceanSpacesServiceImplTest {

    private val s3Client = mock(S3Client::class.java)
    private val storageService = DigitalOceanSpacesServiceImpl(s3Client, "mt-mls-img-storage")

    @Test
    fun uploadShouldStoreObjectUnderNamespacePrefix() {
        val storageKey = storageService.upload(
            namespace = "maeva-app-1",
            filename = "photo.jpg",
            contentType = "image/jpeg",
            data = "fake-image-bytes".toByteArray(),
        )

        assertTrue(storageKey.startsWith("maeva-app-1/"))
        assertTrue(storageKey.endsWith(".jpg"))

        val requestCaptor = ArgumentCaptor.forClass(PutObjectRequest::class.java)
        verify(s3Client).putObject(requestCaptor.capture(), any<RequestBody>())
        assertEquals("mt-mls-img-storage", requestCaptor.value.bucket())
        assertEquals(storageKey, requestCaptor.value.key())
        assertEquals("image/jpeg", requestCaptor.value.contentType())
    }

    @Test
    fun uploadShouldKeepKeyWithoutExtensionWhenFilenameHasNone() {
        val storageKey = storageService.upload(
            namespace = "maeva-app-1",
            filename = "noextension",
            contentType = "application/octet-stream",
            data = "data".toByteArray(),
        )

        assertTrue(storageKey.startsWith("maeva-app-1/"))
        assertTrue(!storageKey.endsWith("."))
    }

    @Test
    fun downloadShouldReturnStreamFromS3Client() {
        val content = "binary-content".toByteArray()
        val responseStream = ResponseInputStream(
            GetObjectResponse.builder().build(),
            ByteArrayInputStream(content)
        )
        `when`(s3Client.getObject(any<GetObjectRequest>())).thenReturn(responseStream)

        val result = storageService.download("maeva-app-1/2026/07/image.jpg")

        assertEquals("binary-content", result.readBytes().toString(Charsets.UTF_8))

        val requestCaptor = ArgumentCaptor.forClass(GetObjectRequest::class.java)
        verify(s3Client).getObject(requestCaptor.capture())
        assertEquals("mt-mls-img-storage", requestCaptor.value.bucket())
        assertEquals("maeva-app-1/2026/07/image.jpg", requestCaptor.value.key())
    }

    @Test
    fun deleteShouldCallS3ClientWithCorrectKey() {
        storageService.delete("maeva-app-1/2026/07/image.jpg")

        val requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest::class.java)
        verify(s3Client).deleteObject(requestCaptor.capture())
        assertEquals("mt-mls-img-storage", requestCaptor.value.bucket())
        assertEquals("maeva-app-1/2026/07/image.jpg", requestCaptor.value.key())
    }
}
