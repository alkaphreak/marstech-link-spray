package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewOcrControllerTest {

    @Test
    fun shouldRenderOcrView() {
        // When
        val result = ViewOcrController().home()

        // Then
        assertEquals(ViewNameEnum.OCR.viewName, result.viewName)
        assertEquals(ViewNameEnum.OCR, result.model["viewNameEnum"])
    }
}
