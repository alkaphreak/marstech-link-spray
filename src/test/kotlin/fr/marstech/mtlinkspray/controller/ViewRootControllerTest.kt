package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.view.ViewRootController
import fr.marstech.mtlinkspray.enums.ViewNameEnum.HOME
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