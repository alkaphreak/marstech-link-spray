package fr.marstech.mtlinkspray.adapter.inbound.web.nativetools
import fr.marstech.mtlinkspray.domain.shared.BusyException
import fr.marstech.mtlinkspray.domain.shared.QuotaExceededException
import fr.marstech.mtlinkspray.MtLinkSprayApplication
import fr.marstech.mtlinkspray.config.SecurityConfig
import fr.marstech.mtlinkspray.adapter.inbound.web.common.GlobalRestExceptionHandler
import fr.marstech.mtlinkspray.domain.shared.PayloadTooLargeException
import fr.marstech.mtlinkspray.domain.nativetools.OcrResult
import fr.marstech.mtlinkspray.domain.nativetools.OcrService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
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

@WebMvcTest(controllers = [OcrApiController::class])
@ContextConfiguration(classes = [MtLinkSprayApplication::class])
@Import(GlobalRestExceptionHandler::class, SecurityConfig::class)
class OcrApiControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var ocrService: OcrService

    private fun givenRequest() = post("/api/ocr")
        .contentType(MediaType.IMAGE_PNG)
        .header("CF-Connecting-IP", "203.0.113.7")
        .content(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47))

    private fun givenFailure(e: Exception) {
        doThrow(e).whenever(ocrService).recognize(anyOrNull(), anyOrNull(), any(), any())
    }

    @Test
    fun shouldReturnRecognizedTextForAnonymousCaller() {
        // Given
        whenever(ocrService.recognize(isNull(), eq("203.0.113.7"), any(), eq("fra")))
            .thenReturn(OcrResult("Évolution du projet", "fra", 420))

        // When / Then
        mockMvc.perform(givenRequest())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.text").value("Évolution du projet"))
            .andExpect(jsonPath("$.lang").value("fra"))
            .andExpect(jsonPath("$.processingTimeMs").value(420))
    }

    @Test
    fun shouldAcceptPdfBody() {
        // Given
        whenever(ocrService.recognize(anyOrNull(), anyOrNull(), any(), any())).thenReturn(OcrResult("scan", "fra", 1))

        // When / Then
        mockMvc.perform(givenRequest().contentType(MediaType.APPLICATION_PDF).content("%PDF-".toByteArray()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.text").value("scan"))
    }

    @Test
    fun shouldPassApiKeyAndLanguage() {
        // Given
        whenever(ocrService.recognize(anyOrNull(), anyOrNull(), any(), any())).thenReturn(OcrResult("text", "eng", 1))

        // When
        mockMvc.perform(givenRequest().header("X-Api-Key", "key123.secret").param("lang", "eng"))
            .andExpect(status().isOk)

        // Then
        verify(ocrService).recognize(eq("key123.secret"), eq("203.0.113.7"), any(), eq("eng"))
    }

    @Test
    fun shouldReturnUnauthorizedWhenApiKeyIsInvalid() {
        // Given
        givenFailure(IllegalAccessException("Invalid API key"))

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isUnauthorized)
    }

    @Test
    fun shouldReturnTooManyRequestsWhenQuotaIsSpent() {
        // Given
        givenFailure(QuotaExceededException("Anonymous OCR quota of 10 reached", 30))

        // When / Then
        mockMvc.perform(givenRequest())
            .andExpect(status().isTooManyRequests)
            .andExpect(jsonPath("$.message").value("Anonymous OCR quota of 10 reached"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Retry-After", "30"))
    }

    @Test
    fun shouldReturnServiceUnavailableWhenEngineIsBusy() {
        // Given
        givenFailure(BusyException("OCR engine is not available"))

        // When / Then
        mockMvc.perform(givenRequest())
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.error").value("Service Unavailable"))
            .andExpect(jsonPath("$.message").value("OCR engine is not available"))
    }

    @Test
    fun shouldReturnContentTooLargeWhenImageExceedsLimit() {
        // Given
        givenFailure(PayloadTooLargeException("too large"))

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isPayloadTooLarge)
    }

    @Test
    fun shouldReturnBadRequestWhenBodyIsNotAnImage() {
        // Given
        givenFailure(IllegalArgumentException("Body is not a PNG or JPEG image"))

        // When / Then
        mockMvc.perform(givenRequest()).andExpect(status().isBadRequest)
    }

    @Test
    fun shouldRejectUnsupportedContentType() {
        // When / Then
        mockMvc.perform(givenRequest().contentType(MediaType.IMAGE_GIF))
            .andExpect(status().isUnsupportedMediaType)
    }
}
