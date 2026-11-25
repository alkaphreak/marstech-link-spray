package fr.marstech.mtlinkspray.domain.image
import fr.marstech.mtlinkspray.domain.shared.ForbiddenException
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.ApiCaller
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream

class ImageServiceImplTest {

    private val imageStore = mock(ImageStore::class.java)
    private val apiKeyResolver = mock(ApiKeyResolver::class.java)
    private val imageStorageService = mock(ImageStorageService::class.java)
    private val client = ClientContext(ipAddress = "203.0.113.7")

    private fun upload(filename: String, bytes: ByteArray) = ImageUpload(filename, bytes.size.toLong()) { bytes.inputStream() }

    private val maxFileSizeBytes = 25L * 1024 * 1024 // 25 MB, same default as production config
    private val imageService = ImageServiceImpl(
        imageStore, apiKeyResolver, imageStorageService, maxFileSizeBytes
    )

    private val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00)

    private val apiKey = ApiCaller(apiKeyId = "apikey-1", namespace = "maeva-app-1")

    private fun givenImage(
        id: String = "img-1",
        apiKeyId: String = apiKey.apiKeyId,
        isPrivate: Boolean = false,
    ): Image = Image(
        id = id,
        apiKeyId = apiKeyId,
        storageKey = "maeva-app-1/2026/07/$id.jpg",
        originalFilename = "photo.jpg",
        contentType = "image/jpeg",
        sizeBytes = 1234L,
        isPrivate = isPrivate,
        authorIpAddress = "127.0.0.1",
    )

    @Test
    fun uploadImageShouldStoreFileAndSaveMetadata() {
        val file = upload("photo.jpg", jpegBytes)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)
        whenever(imageStorageService.upload(any(), any(), any(), any()))
            .thenReturn("maeva-app-1/2026/07/generated.jpg")
        whenever(imageStore.create(any())).thenAnswer { it.arguments[0] }

        val result = imageService.uploadImage("key123.secret", file, false, client)

        assertEquals("maeva-app-1/2026/07/generated.jpg", result.storageKey)
        assertEquals("photo.jpg", result.originalFilename)
        assertEquals("image/jpeg", result.contentType)
        assertEquals(apiKey.apiKeyId, result.apiKeyId)
        assertEquals("203.0.113.7", result.authorIpAddress)
        assertEquals(jpegBytes.size.toLong(), result.sizeBytes)
        verify(imageStorageService).upload(eq("maeva-app-1"), eq("photo.jpg"), eq("image/jpeg"), any())
        verify(imageStore).create(any())
    }

    @Test
    fun uploadImageShouldThrowWhenApiKeyIsInvalid() {
        val file = upload("photo.jpg", "bytes".toByteArray())
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Invalid API key") }
            .`when`(apiKeyResolver).resolve("bad-key")

        assertThrows<IllegalAccessException> {
            imageService.uploadImage("bad-key", file, false, client)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldRejectHtmlDisguisedAsJpeg() {
        val file = upload("photo.jpg", "<script>alert(1)</script>".toByteArray())
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, client)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldRejectSvg() {
        val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" onload=\"alert(1)\"/>".toByteArray()
        val file = upload("logo.svg", svg)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, client)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldStoreDetectedTypeInsteadOfClientType() {
        val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00)
        val file = upload("photo.jpg", pngBytes)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)
        whenever(imageStorageService.upload(any(), any(), any(), any())).thenReturn("maeva-app-1/x.png")
        whenever(imageStore.create(any())).thenAnswer { it.arguments[0] }

        val result = imageService.uploadImage("key123.secret", file, false, client)

        assertEquals("image/png", result.contentType)
    }

    @Test
    fun uploadImageShouldThrowWhenFileIsEmpty() {
        val file = upload("empty.jpg", ByteArray(0))
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, client)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun uploadImageShouldThrowWhenFileExceedsMaxSize() {
        val oversizedContent = ByteArray((maxFileSizeBytes + 1).toInt())
        val file = upload("big.jpg", oversizedContent)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<IllegalArgumentException> {
            imageService.uploadImage("key123.secret", file, false, client)
        }
        verifyNoInteractions(imageStorageService)
    }

    @Test
    fun getImageShouldReturnPublicImageWithoutApiKey() {
        val image = givenImage(isPrivate = false)
        `when`(imageStore.findById("img-1")).thenReturn(image)

        val result = imageService.getImage("img-1", null)

        assertEquals(image, result)
        verifyNoInteractions(apiKeyResolver)
    }

    @Test
    fun getImageShouldThrowNotFoundWhenImageDoesNotExist() {
        `when`(imageStore.findById("missing")).thenReturn(null)

        assertThrows<NoSuchElementException> {
            imageService.getImage("missing", null)
        }
    }

    @Test
    fun getImageShouldReturnPrivateImageWhenOwnerApiKeyMatches() {
        val image = givenImage(isPrivate = true)
        `when`(imageStore.findById("img-1")).thenReturn(image)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        val result = imageService.getImage("img-1", "key123.secret")

        assertEquals(image, result)
    }

    @Test
    fun getImageShouldThrowWhenPrivateImageAccessedWithoutApiKey() {
        val image = givenImage(isPrivate = true)
        `when`(imageStore.findById("img-1")).thenReturn(image)
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Missing API key") }
            .`when`(apiKeyResolver).resolve(null)

        assertThrows<IllegalAccessException> {
            imageService.getImage("img-1", null)
        }
    }

    @Test
    fun getImageShouldThrowWhenPrivateImageAccessedByDifferentApp() {
        val image = givenImage(isPrivate = true, apiKeyId = "other-app-id")
        `when`(imageStore.findById("img-1")).thenReturn(image)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<ForbiddenException> {
            imageService.getImage("img-1", "key123.secret")
        }
    }

    @Test
    fun downloadImageShouldReturnImageAndStream() {
        val image = givenImage(isPrivate = false)
        `when`(imageStore.findById("img-1")).thenReturn(image)
        `when`(imageStorageService.download(image.storageKey)).thenReturn(ByteArrayInputStream("data".toByteArray()))

        val (resultImage, stream) = imageService.downloadImage("img-1", null)

        assertEquals(image, resultImage)
        assertEquals("data", stream.readBytes().toString(Charsets.UTF_8))
    }

    @Test
    fun deleteImageShouldRemoveFromStorageAndRepositoryWhenOwnedByApp() {
        val image = givenImage()
        `when`(imageStore.findById("img-1")).thenReturn(image)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        imageService.deleteImage("img-1", "key123.secret")

        verify(imageStorageService).delete(image.storageKey)
        verify(imageStore).deleteById("img-1")
    }

    @Test
    fun deleteImageShouldThrowWhenNotOwnedByApp() {
        val image = givenImage(apiKeyId = "other-app-id")
        `when`(imageStore.findById("img-1")).thenReturn(image)
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)

        assertThrows<ForbiddenException> {
            imageService.deleteImage("img-1", "key123.secret")
        }
        verify(imageStorageService, never()).delete(any())
        verify(imageStore, never()).deleteById(any())
    }

    @Test
    fun listImagesShouldReturnImagesForResolvedApiKey() {
        val images = listOf(givenImage(id = "img-1"), givenImage(id = "img-2"))
        `when`(apiKeyResolver.resolve("key123.secret")).thenReturn(apiKey)
        `when`(imageStore.findByApiKeyId(apiKey.apiKeyId)).thenReturn(images)

        val result = imageService.listImages("key123.secret")

        assertEquals(images, result)
    }
}
