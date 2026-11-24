package fr.marstech.mtlinkspray.adapter.inbound.web.image
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus.FORBIDDEN
import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.config.SecurityConfig
import fr.marstech.mtlinkspray.adapter.inbound.web.common.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.domain.image.Image
import fr.marstech.mtlinkspray.domain.image.ImageService
import fr.marstech.mtlinkspray.domain.image.ImageUpload
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.io.ByteArrayInputStream

@WebMvcTest(controllers = [ImageApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class ImageApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var imageService: ImageService

    private fun givenImage(
        id: String = "img-1",
        isPrivate: Boolean = false,
    ): Image = Image(
        id = id,
        apiKeyId = "apikey-1",
        storageKey = "maeva-app-1/2026/07/$id.jpg",
        originalFilename = "photo.jpg",
        contentType = "image/jpeg",
        sizeBytes = 1234L,
        isPrivate = isPrivate,
        authorIpAddress = "127.0.0.1",
    )

    @Test
    fun shouldUploadImageAndReturnMetadata() {
        val image = givenImage()
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-bytes".toByteArray())
        `when`(imageService.uploadImage(any(), any(), any(), any())).thenReturn(image)

        multipart("/api/images")
            .file(file)
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(image.id))
            .andExpect(jsonPath("$.originalFilename").value("photo.jpg"))
            .andExpect(jsonPath("$.contentType").value("image/jpeg"))
            .andExpect(jsonPath("$.isPrivate").value(false))
            .andExpect(jsonPath("$.sizeBytes").value(image.sizeBytes))
            .andExpect(jsonPath("$.creationDate").exists())

        val upload = argumentCaptor<ImageUpload>()
        verify(imageService).uploadImage(eq("key123.secret"), upload.capture(), eq(false), any())
        assertEquals("photo.jpg", upload.firstValue.originalFilename)
        assertEquals(10L, upload.firstValue.sizeBytes)
        assertEquals("fake-bytes", upload.firstValue.content().readAllBytes().decodeToString())
    }

    @Test
    fun shouldReturnUnauthorizedWhenApiKeyIsInvalidOnUpload() {
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-bytes".toByteArray())
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Invalid API key") }
            .`when`(imageService).uploadImage(any(), any(), any(), any())

        multipart("/api/images")
            .file(file)
            .header("X-Api-Key", "bad-key")
            .let(mockMvc::perform)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    fun shouldReturnContentTooLargeWhenUploadExceedsLimit() {
        val file = MockMultipartFile("file", "photo.jpg", "image/jpeg", "fake-bytes".toByteArray())
        org.mockito.kotlin.doAnswer { throw org.springframework.web.multipart.MaxUploadSizeExceededException(-1) }
            .`when`(imageService).uploadImage(any(), any(), any(), any())

        multipart("/api/images")
            .file(file)
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isPayloadTooLarge)
            .andExpect(jsonPath("$.message").value("File exceeds the maximum upload size of 25 MB"))
    }

    @Test
    fun shouldReturnBadRequestWhenUploadIsNotMultipart() {
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/images")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldReturnBadRequestWhenFilePartIsMissing() {
        multipart("/api/images")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldListImagesForApiKey() {
        val images = listOf(givenImage(id = "img-1"), givenImage(id = "img-2"))
        `when`(imageService.listImages("key123.secret")).thenReturn(images)

        get("/api/images")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("img-1"))
            .andExpect(jsonPath("$[1].id").value("img-2"))
    }

    @Test
    fun shouldStreamPublicImageWithoutApiKey() {
        val image = givenImage(isPrivate = false)
        val content = "binary-content"
        `when`(imageService.downloadImage("img-1", null))
            .thenReturn(image to ByteArrayInputStream(content.toByteArray()))

        get("/api/images/img-1")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("image/jpeg"))
            .andExpect(header().string("Content-Disposition", "inline; filename=\"photo.jpg\""))
            .andExpect(header().string("Cache-Control", "public, max-age=86400, s-maxage=300"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(content().bytes(content.toByteArray()))
    }

    @Test
    fun shouldForbidSharedCachingOfPrivateImage() {
        val image = givenImage(isPrivate = true)
        `when`(imageService.downloadImage("img-1", "key123.secret"))
            .thenReturn(image to ByteArrayInputStream("binary-content".toByteArray()))

        get("/api/images/img-1")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(header().string("Cache-Control", "private, no-store"))
    }

    @Test
    fun shouldForceDownloadForStoredNonImageType() {
        val image = givenImage().copy(contentType = "text/html", originalFilename = "x\".html")
        `when`(imageService.downloadImage("img-1", null))
            .thenReturn(image to ByteArrayInputStream("<script>".toByteArray()))

        get("/api/images/img-1")
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/octet-stream"))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.startsWith("attachment;")))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
    }

    @Test
    fun shouldReturnUnauthorizedForPrivateImageWithoutApiKey() {
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Missing API key") }
            .`when`(imageService).downloadImage("img-1", null)

        get("/api/images/img-1")
            .let(mockMvc::perform)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    fun shouldReturnNotFoundForMissingImage() {
        `when`(imageService.downloadImage("missing", null))
            .thenThrow(NoSuchElementException("Image missing not found"))

        get("/api/images/missing")
            .let(mockMvc::perform)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun shouldDeleteImageWhenOwnedByApiKey() {
        delete("/api/images/img-1")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isNoContent)
    }

    @Test
    fun shouldReturnForbiddenWhenDeletingImageNotOwned() {
        org.mockito.kotlin.doAnswer { throw ResponseStatusException(FORBIDDEN, "Not authorized to delete this image") }
            .`when`(imageService).deleteImage("img-1", "key123.secret")

        delete("/api/images/img-1")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }
}
