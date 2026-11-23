package fr.marstech.mtlinkspray.adapter.inbound.web.shortener
import fr.marstech.mtlinkspray.domain.shortener.SprayService
import jakarta.servlet.http.HttpServletRequest
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NotEmptyNotBlank
import fr.marstech.mtlinkspray.adapter.inbound.web.common.ValidUrlList

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/spray")
class SprayApiController(
    private val sprayService: SprayService
) {

    @GetMapping
    fun getSprayLink(
        httpServletRequest: HttpServletRequest,
        @RequestParam
        @NotEmptyNotBlank
        @ValidUrlList(maxSize = 100, message = "Invalid URL list")
        inputLinkList: List<String>,
        @RequestParam(defaultValue = "false")
        shorten: Boolean = false,
    ): String =
        if (shorten) {
            sprayService.shortenAndSpray(inputLinkList, httpServletRequest)
        } else {
            SprayService.getLinkSpray(httpServletRequest, inputLinkList)
        }
}
