package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.ApiKeyEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.ImageEntity
import fr.marstech.mtlinkspray.repository.ImageRepository
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayInputStream
import java.util.*

class ImageServiceImplTest {

    private val imageRepository = mock(ImageRepository::class.java)
    private val apiKeyService = mock(ApiKeyService::class.java)
    private val imageStorageService = mock(ImageStorageService::class.java)
    private val httpServletRequest = mock(HttpServletRequest::class.java)

    private val maxFileSizeBytes = 25L * 1024 * 1024 // 25 MB, same default as production config
    private val imageService = ImageServiceImpl(
        imageRepository, apiKeyService, imageStorageService, maxFileSizeBytes
    )

    private val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00)

    private val apiKey = ApiKeyEntity(
        id = "apikey-1",
        name = "Maeva App",
        keyId = "key123",
        secretHash = "hashed",
        namespace = "maeva-app-1",
    )

    private fun givenImage(
        id: String = "img-1",
        apiKeyId: String = apiKey.id,
        isPrivate: Boolean = false,
    ): ImageEntity = ImageEntity(
        id = id,
        apiKeyId = apiKeyId,
        s3Key = "maeva-app-1/2026/07/$id.jpg",
        originalFilename = "photo.jpg",
        contentType = "image/jpeg",
        sizeBytes = 1234L,
        isPrivate = isPrivate,
        author = HistoryItem(ipAddress = "127.0.0.1", action = "UPLOAD_IMAGE"),
    )

    @Test
    fun uploadImageShouldStoreFileAndSaveMetadata() {
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", jpegBytes)
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)
        whenever(imageStorageService.upload(any(), any(), any(), any()))
            .thenReturn("maeva-app-1/2026/07/generated.jpg")
        `when`(imageRepository.save(any(ImageEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = imageService.uploadImage("key123.secret", file, false, httpServletRequest)

        assertEquals("maeva-app-1/2026/07/generated.jpg", result.s3Key)
        assertEquals("photo.jpg", result.originalFilename)
        assertEquals("image/jpeg", result.contentType)
        assertEquals(apiKey.id, result.apiKeyId)
        verify(imageStorageService).upload(eq("maeva-app-1"), eq("photo.jpg"), eq("image/jpeg"), any())
        verify(imageRepository).save(any(ImageEntity::class.java))
    }

    @Test
    fun uploadImageShouldThrowWhenApiKeyIsInvalid() {
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", "bytes".toByteArray())
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Invalid API key") }
            .`when`(apiKeyService).resolve("bad-key")

        assertThrows<IllegalAccessException> {
            imageService.uploadImage("bad-key", file, false, httpServletRequest)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldRejectHtmlDisguisedAsJpeg() {
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", "<script>alert(1)</script>".toByteArray())
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, httpServletRequest)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldRejectSvg() {
        val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>".toByteArray()
        val file = MockMultipartFile("file", "logo.svg", "image/svg+xml", svg)
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, httpServletRequest)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldStoreDetectedTypeInsteadOfClientType() {
        val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00)
        val file = MockMultipartFile("file", "photo.jpg", "text/html", pngBytes)
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)
        whenever(imageStorageService.upload(any(), any(), any(), any())).thenReturn("maeva-app-1/x.png")
        `when`(imageRepository.save(any(ImageEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = imageService.uploadImage("key123.secret", file, false, httpServletRequest)

        assertEquals("image/png", result.contentType)
    }

    @Test
    fun uploadImageShouldThrowWhenFileIsEmpty() {
        val file = MockMultipartFile("file", "empty.jpg", "image/jpeg", ByteArray(0))
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, httpServletRequest)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldThrowWhenFileExceedsMaxSize() {
        val oversizedContent = ByteArray((maxFileSizeBytes + 1).toInt())
        val file = MockMultipartFile("file", "big.jpg", "image/jpeg", oversizedContent)
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, httpServletRequest)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun getImageShouldReturnPublicImageWithoutApiKey() {
        val image = givenImage(isPrivate = false)
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))

        val result = imageService.getImage("img-1", null)

        assertEquals(image, result)
        verifyNoInteractions(apiKeyService)
    }

    @Test
    fun getImageShouldThrowNotFoundWhenImageDoesNotExist() {
        `when`(imageRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            imageService.getImage("missing", null)
        }
    }

    @Test
    fun getImageShouldReturnPrivateImageWhenOwnerApiKeyMatches() {
        val image = givenImage(isPrivate = true)
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        val result = imageService.getImage("img-1", "key123.secret")

        assertEquals(image, result)
    }

    @Test
    fun getImageShouldThrowWhenPrivateImageAccessedWithoutApiKey() {
        val image = givenImage(isPrivate = true)
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Missing API key") }
            .`when`(apiKeyService).resolve(null)

        assertThrows<IllegalAccessException> {
            imageService.getImage("img-1", null)
        }
    }

    @Test
    fun getImageShouldThrowWhenPrivateImageAccessedByDifferentApp() {
        val image = givenImage(isPrivate = true, apiKeyId = "other-app-id")
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalAccessException> {
            imageService.getImage("img-1", "key123.secret")
        }
    }

    @Test
    fun downloadImageShouldReturnImageAndStream() {
        val image = givenImage(isPrivate = false)
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))
        `when`(imageStorageService.download(image.s3Key)).thenReturn(ByteArrayInputStream("data".toByteArray()))

        val (resultImage, stream) = imageService.downloadImage("img-1", null)

        assertEquals(image, resultImage)
        assertEquals("data", stream.readBytes().toString(Charsets.UTF_8))
    }

    @Test
    fun deleteImageShouldRemoveFromStorageAndRepositoryWhenOwnedByApp() {
        val image = givenImage()
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        imageService.deleteImage("img-1", "key123.secret")

        verify(imageStorageService).delete(image.s3Key)
        verify(imageRepository).deleteById("img-1")
    }

    @Test
    fun deleteImageShouldThrowWhenNotOwnedByApp() {
        val image = givenImage(apiKeyId = "other-app-id")
        `when`(imageRepository.findById("img-1")).thenReturn(Optional.of(image))
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalAccessException> {
            imageService.deleteImage("img-1", "key123.secret")
        }
        verify(imageStorageService, never()).delete(any())
        verify(imageRepository, never()).deleteById(any())
    }

    @Test
    fun listImagesShouldReturnImagesForResolvedApiKey() {
        val images = listOf(givenImage(id = "img-1"), givenImage(id = "img-2"))
        `when`(apiKeyService.resolve("key123.secret")).thenReturn(apiKey)
        `when`(imageRepository.findByApiKeyId(apiKey.id)).thenReturn(images)

        val result = imageService.listImages("key123.secret")

        assertEquals(images, result)
    }
}
