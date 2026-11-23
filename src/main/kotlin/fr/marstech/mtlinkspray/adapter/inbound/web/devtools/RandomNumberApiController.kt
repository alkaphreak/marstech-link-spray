package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.domain.devtools.RandomNumberService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/random")
class RandomNumberApiController(
    private val randomNumberService: RandomNumberService
) {

    @GetMapping
    fun getRandom(
        @RequestParam(name = "min", required = false) min: String?,
        @RequestParam(name = "max", required = false) max: String?
    ): RandomNumberResponse = randomNumberService.generate(min, max)
}
