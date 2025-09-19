package fr.marstech.mtlinkspray.controller

import fr.marstech.mtlinkspray.controller.api.ApiUuidController
import fr.marstech.mtlinkspray.enums.ViewNameEnum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ViewUuidControllerTest {

    @Mock
    lateinit var apiUuidController: ApiUuidController

    @InjectMocks
    lateinit var viewUuidController: ViewUuidController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testGetHome() {
        `when`(apiUuidController.uuid).thenReturn("getUuidResponse")
        val result = viewUuidController.home()
        assertEquals(ViewNameEnum.UUID.viewName, result.viewName)
        assertEquals("getUuidResponse", result.model["uuid"])
        assertEquals(ViewNameEnum.UUID, result.model["viewNameEnum"])
    }

    @Test
    fun testGetModelAndView() {
        val result = viewUuidController.getModelAndView()
        assertEquals(ViewNameEnum.UUID.viewName, result.viewName)
        assertEquals(ViewNameEnum.UUID, result.model["viewNameEnum"])
    }

    @Test
    fun testGetModelAndView2() {
        val result = viewUuidController.getModelAndView(ViewNameEnum.HOME)
        assertEquals(ViewNameEnum.HOME.viewName, result.viewName)
        assertEquals(ViewNameEnum.HOME, result.model["viewNameEnum"])
    }
}