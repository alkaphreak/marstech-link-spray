package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RandomViewControllerTest {

    private val controller = RandomViewController()

    @Test
    fun shouldProvideModelAndViewWithViewNameEnum() {
        val mav = controller.getModelAndView()
        assertNotNull(mav)
        assertEquals(ViewNameEnum.RANDOM.viewName, mav.viewName)
        assertTrue(mav.model.containsKey("viewNameEnum"))
        val attr = mav.model["viewNameEnum"]
        assertNotNull(attr)
        assertTrue(attr is ViewNameEnum)
        assertEquals(ViewNameEnum.RANDOM, attr)
    }
}
