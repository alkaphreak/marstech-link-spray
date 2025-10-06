package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.ShortenerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/url-shortener")
class ShortenerApiController(
    val shortenerService: ShortenerService
) {

    @GetMapping("/shorten")
    @Validated
    fun getShort(
        @RequestParam(name = "url") @Valid @NotBlank(message = "URL cannot be blank") inputUrl: String,
        httpServletRequest: HttpServletRequest
    ): String = when {
        // In some case the validation may be bypassed
        inputUrl.isNotBlank() -> shortenerService.shorten(inputUrl, httpServletRequest)
        else -> throw IllegalArgumentException("URL cannot be blank")
    }
}
