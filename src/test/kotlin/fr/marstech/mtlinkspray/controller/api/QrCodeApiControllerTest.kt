package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.service.QrCodeService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [QrCodeApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class QrCodeApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var qrCodeService: QrCodeService

    @Test
    fun shouldReturnPngByDefault() {
        // Given
        `when`(qrCodeService.render(any())).thenReturn(byteArrayOf(1, 2, 3))

        // When / Then
        mockMvc.perform(get("/api/qr").param("text", "hello"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("image/png"))
            .andExpect(content().bytes(byteArrayOf(1, 2, 3)))
            .andExpect(header().doesNotExist("Content-Disposition"))
    }

    @Test
    fun shouldReturnSvgAttachmentWhenDownloadRequested() {
        // Given
        `when`(qrCodeService.render(any())).thenReturn("<svg/>".toByteArray())

        // When / Then
        mockMvc.perform(get("/api/qr").param("text", "hello").param("format", "svg").param("download", "true"))
            .andExpect(status().isOk)
            .andExpect(content().contentType("image/svg+xml"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"qrcode.svg\""))
    }

    @Test
    fun shouldReturnBadRequestWhenTextMissing() {
        mockMvc.perform(get("/api/qr"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("text"))
    }

    @Test
    fun shouldReturnBadRequestWhenColorInvalid() {
        mockMvc.perform(get("/api/qr").param("text", "hello").param("fg", "red"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("fg"))
    }

    @Test
    fun shouldReturnBadRequestWhenSizeOutOfRange() {
        mockMvc.perform(get("/api/qr").param("text", "hello").param("size", "4096"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("size"))
    }

    @Test
    fun shouldReturnBadRequestWhenFormatUnsupported() {
        mockMvc.perform(get("/api/qr").param("text", "hello").param("format", "gif"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors[0].field").value("format"))
    }

    @Test
    fun shouldReturnBadRequestWhenTextTooLongForQrCode() {
        // Given
        `when`(qrCodeService.render(any())).thenThrow(IllegalArgumentException("Text is too long for a QR code"))

        // When / Then
        mockMvc.perform(get("/api/qr").param("text", "hello"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Text is too long for a QR code"))
    }
}
