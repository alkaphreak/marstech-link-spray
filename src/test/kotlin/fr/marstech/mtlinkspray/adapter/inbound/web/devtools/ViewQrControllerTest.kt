package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum
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
