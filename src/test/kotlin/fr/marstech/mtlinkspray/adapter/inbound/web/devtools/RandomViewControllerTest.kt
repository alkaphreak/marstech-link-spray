package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.domain.devtools.RandomNumber
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ViewNameEnum
import fr.marstech.mtlinkspray.domain.devtools.RandomNumberService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant

class RandomViewControllerTest {

    private val randomNumberService = mock(RandomNumberService::class.java)
    private val controller = RandomViewController(randomNumberService)

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

    @Test
    fun viewRandomShouldAddRandomNumberToModel() {
        // Given
        val response = RandomNumber(value = 42, min = 0, max = 100, timestamp = Instant.now())
        `when`(randomNumberService.generate(RandomViewController.DEFAULT_MIN, RandomViewController.DEFAULT_MAX))
            .thenReturn(response)

        // When
        val mav = controller.viewRandom()

        // Then
        assertEquals(ViewNameEnum.RANDOM.viewName, mav.viewName)
        assertEquals(42, mav.model["randomNumber"])
        assertEquals(RandomViewController.DEFAULT_MIN, mav.model["defaultMin"])
        assertEquals(RandomViewController.DEFAULT_MAX, mav.model["defaultMax"])
    }
}
