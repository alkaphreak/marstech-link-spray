package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.ShortenerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/url-shortener")
open class ShortenerApiController(
    open val shortenerService: ShortenerService
) {

    @GetMapping("/shorten")
    fun getShort(
        @RequestParam(name = "url") inputUrl: @NotBlank String,
        httpServletRequest: HttpServletRequest
    ): String = shortenerService.shorten(
        inputUrl,
        httpServletRequest
    )
}
