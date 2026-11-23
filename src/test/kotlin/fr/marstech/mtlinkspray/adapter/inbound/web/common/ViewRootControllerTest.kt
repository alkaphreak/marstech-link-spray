package fr.marstech.mtlinkspray.adapter.inbound.web.common
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum.HOME
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ViewRootControllerTest {

    private val controller = ViewRootController()

    @Test
    fun getHomeReturnsExpectedView() {
        val result = controller.getHome()
        assertEquals(HOME.viewName, result.viewName)
    }

    @Test
    fun getIndexReturnsExpectedView() {
        val result = controller.getIndex()
        assertEquals("redirect:/", result)
    }
}