package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewQrController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ViewQrControllerTest {

    private val viewQrController = ViewQrController()

    @Test
    fun shouldReturnQrView() {
        val result = viewQrController.home()
        assertEquals(ViewNameEnum.QR.viewName, result.viewName)
        assertEquals(ViewNameEnum.QR, result.model["viewNameEnum"])
    }
}
