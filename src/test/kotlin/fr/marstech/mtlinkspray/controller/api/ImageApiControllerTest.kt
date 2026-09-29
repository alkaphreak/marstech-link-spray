package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.ImageEntity
import fr.marstech.mtlinkspray.service.ImageService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
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
    ): ImageEntity = ImageEntity(
        id = id,
        apiKeyId = "apikey-1",
        s3Key = "maeva-app-1/2026/07/$id.jpg",
        originalFilename = "photo.jpg",
        contentType = "image/jpeg",
        sizeBytes = 1234L,
        isPrivate = isPrivate,
        author = HistoryItem(ipAddress = "127.0.0.1", action = "UPLOAD_IMAGE"),
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
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(image.id))
            .andExpect(jsonPath("$.originalFilename").value("photo.jpg"))
            .andExpect(jsonPath("$.contentType").value("image/jpeg"))
            .andExpect(jsonPath("$.isPrivate").value(false))
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
            .andExpect(header().string("Cache-Control", "max-age=86400"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(content().bytes(content.toByteArray()))
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
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Not authorized to access this image") }
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
    fun shouldReturnUnauthorizedWhenDeletingImageNotOwned() {
        org.mockito.kotlin.doAnswer { throw IllegalAccessException("Not authorized to delete this image") }
            .`when`(imageService).deleteImage("img-1", "key123.secret")

        delete("/api/images/img-1")
            .header("X-Api-Key", "key123.secret")
            .let(mockMvc::perform)
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }
}
