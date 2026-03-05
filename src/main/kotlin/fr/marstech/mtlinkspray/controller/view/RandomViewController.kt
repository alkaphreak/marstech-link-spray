package fr.marstech.mtlinkspray.controller.view

import fr.marstech.mtlinkspray.enums.ViewNameEnum.RANDOM
import fr.marstech.mtlinkspray.service.RandomNumberService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/random")
class RandomViewController(
    private val randomNumberService: RandomNumberService
) : ThymeleafViewControllerInterface {

    override fun getModelAndView(): ModelAndView = getModelAndView(RANDOM)

    @GetMapping
    fun viewRandom(): ModelAndView = getModelAndView()
        .addObject("defaultMin", DEFAULT_MIN)
        .addObject("defaultMax", DEFAULT_MAX)
        .addObject(
            "randomNumber",
            randomNumberService.generateRandom(DEFAULT_MIN, DEFAULT_MAX).value
        )

    companion object {
        const val DEFAULT_MIN = "0"
        const val DEFAULT_MAX = "100"
    }
}
