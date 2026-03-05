package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.controller.api.RandomNumberApiController
import fr.marstech.mtlinkspray.enums.ViewNameEnum.RANDOM
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/random")
class RandomViewController(
    private val randomNumberApiController: RandomNumberApiController
) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(RANDOM)

    @GetMapping
    fun viewRandom(): ModelAndView = getModelAndView()
        .addObject("defaultMin", DEFAULT_MIN)
        .addObject("defaultMax", DEFAULT_MAX)
        .addObject(
            "randomNumber",
            randomNumberApiController.getRandom(DEFAULT_MIN, DEFAULT_MAX).value
        )

    companion object {
        const val DEFAULT_MIN = "0"
        const val DEFAULT_MAX = "100"
    }
}
