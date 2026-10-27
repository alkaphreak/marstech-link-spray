package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.conf.SecurityConfig
import fr.marstech.mtlinkspray.controller.commons.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.service.QrCodeService
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

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

    @Test
    fun shouldReturnZipWithOneEntryPerNonBlankLine() {
        // Given
        `when`(qrCodeService.render(any())).thenReturn(byteArrayOf(7))

        // When
        val body = mockMvc.perform(
            post("/api/qr/bulk").param("text", "https://sol4.space\n\n  hello world  \n").param("format", "svg")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType("application/zip"))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"qrcodes.zip\""))
            .andReturn().response.contentAsByteArray

        // Then
        val names = ZipInputStream(ByteArrayInputStream(body)).use { zip ->
            generateSequence { zip.nextEntry }.map { it.name }.toList()
        }
        assertEquals(listOf("001-sol4-space.svg", "002-hello-world.svg"), names)
    }

    @Test
    fun shouldReturnBadRequestWhenTooManyLines() {
        val text = (1..QrCodeApiController.MAX_BULK_LINES + 1).joinToString("\n") { "line $it" }

        mockMvc.perform(post("/api/qr/bulk").param("text", text))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("At most 100 lines are accepted, got 101"))
    }

    @Test
    fun shouldReturnBadRequestWhenBulkTextOnlyBlankLines() {
        mockMvc.perform(post("/api/qr/bulk").param("text", " \n \n"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun shouldNameFailingLineWhenBulkLineTooLong() {
        // Given
        `when`(qrCodeService.render(any())).thenReturn(byteArrayOf(1))
            .thenThrow(IllegalArgumentException("Text is too long for a QR code"))

        // When / Then
        mockMvc.perform(post("/api/qr/bulk").param("text", "ok\ntoo long"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Line 2: Text is too long for a QR code"))
    }

    @Test
    fun shouldBuildBulkFilenameWithoutSlugWhenLineHasNoAlphanumeric() {
        assertEquals("007.png", QrCodeApiController.bulkFilename(6, "!!!", "png"))
    }
}
