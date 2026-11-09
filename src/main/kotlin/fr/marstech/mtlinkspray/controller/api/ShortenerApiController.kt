package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.LinkStatsResponse
import fr.marstech.mtlinkspray.dto.ShortenRequest
import fr.marstech.mtlinkspray.dto.ShortenResponse
import fr.marstech.mtlinkspray.service.ShortenerService
import fr.marstech.mtlinkspray.validation.ValidUrl
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Validated
@RestController
@RequestMapping("/api/url-shortener")
class ShortenerApiController(
    val shortenerService: ShortenerService
) {

    @GetMapping("/shorten")
    @Validated
    fun getShort(
        @RequestParam(name = "url")
        @Valid
        @NotBlank(message = "URL cannot be blank")
        @ValidUrl(message = "Invalid URL format or protocol")
        @Size(max = 2048, message = "URL cannot exceed 2048 characters")
        inputUrl: String,
        httpServletRequest: HttpServletRequest
    ): String = shorten(inputUrl, httpServletRequest)

    /** Creates a short link: 201 Created, with the short URL in `Location` and in the body. */
    @PostMapping
    fun createShort(
        @RequestBody @Valid request: ShortenRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<ShortenResponse> =
        shorten(request.url, httpServletRequest)
            .let { ResponseEntity.created(URI.create(it)).body(ShortenResponse(it)) }

    /** Click count of a short link: anonymous, no IP or fingerprint behind it (MLS-261). */
    @GetMapping("/{uid}/stats")
    fun getStats(@PathVariable uid: String): LinkStatsResponse = shortenerService.getStats(uid)

    private fun shorten(inputUrl: String, httpServletRequest: HttpServletRequest): String = when {
        // In some case the validation may be bypassed
        inputUrl.isNotBlank() ->
            shortenerService.shorten(inputUrl, httpServletRequest)
                ?: throw IllegalStateException("Failed to shorten URL")

        else -> throw IllegalArgumentException("URL cannot be blank")
    }
}
