package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.SprayService
import jakarta.servlet.http.HttpServletRequest
import fr.marstech.mtlinkspray.validation.NotEmptyNotBlank

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/spray")
class SprayApiController {

    @GetMapping
    fun getSprayLink(
        httpServletRequest: HttpServletRequest,
        @RequestParam @NotEmptyNotBlank inputLinkList: List<String>,
    ): String =
        SprayService.getLinkSpray(httpServletRequest, inputLinkList)
}
