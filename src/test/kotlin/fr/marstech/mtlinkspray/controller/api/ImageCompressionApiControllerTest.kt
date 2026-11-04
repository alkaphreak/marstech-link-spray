package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.service.CompressedImage
import fr.marstech.mtlinkspray.service.ImageCompressionService
import fr.marstech.mtlinkspray.service.ImageFormat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [ImageCompressionApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class ImageCompressionApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var imageCompressionService: ImageCompressionService

    private fun givenRequest(query: String) = post("/api/img/compress$query")
        .contentType(MediaType.IMAGE_JPEG)
        .header("CF-Connecting-IP", "203.0.113.7")
        .content(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))

    @Test
    fun shouldReturnEncodedImageWithItsTypeAndFlag() {
        // Given
        whenever(imageCompressionService.compress(isNull(), eq("203.0.113.7"), any(), eq(ImageFormat.WEBP), eq(60), eq(800)))
            .thenReturn(CompressedImage("webp".toByteArray(), "image/webp", true))

        // When / Then
        mockMvc.perform(givenRequest("?format=WebP&quality=60&maxDimension=800"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("image/webp"))
            .andExpect(header().string("X-Image-Compressed", "true"))
            .andExpect(content().bytes("webp".toByteArray()))
    }

    @Test
    fun shouldFlagOriginalImageWhenEncoderFailed() {
        // Given
        whenever(imageCompressionService.compress(anyOrNull(), anyOrNull(), any(), eq(ImageFormat.WEBP), eq(75), isNull()))
            .thenReturn(CompressedImage("orig".toByteArray(), "image/jpeg", false))

        // When / Then
        mockMvc.perform(givenRequest(""))
            .andExpect(status().isOk)
            .andExpect(content().contentType("image/jpeg"))
            .andExpect(header().string("X-Image-Compressed", "false"))
    }

    @Test
    fun shouldRejectUnknownFormat() {
        // When / Then
        mockMvc.perform(givenRequest("?format=avif"))
            .andExpect(status().isBadRequest)
        verifyNoInteractions(imageCompressionService)
    }
}
