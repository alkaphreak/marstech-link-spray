package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ViewFingerprintControllerTest {

    @Test
    fun shouldExposeRequestHeadersToTheView() {
        // When
        val result = ViewFingerprintController().home("Mozilla/5.0", "fr-CA,fr;q=0.9")

        // Then
        assertEquals(ViewNameEnum.FINGERPRINT.viewName, result.viewName)
        assertEquals(ViewNameEnum.FINGERPRINT, result.model["viewNameEnum"])
        assertEquals("Mozilla/5.0", result.model["userAgent"])
        assertEquals("fr-CA,fr;q=0.9", result.model["acceptLanguage"])
    }

    @Test
    fun shouldRenderWithoutHeaders() {
        // When
        val result = ViewFingerprintController().home(null, null)

        // Then
        assertNull(result.model["userAgent"])
        assertNull(result.model["acceptLanguage"])
    }
}
