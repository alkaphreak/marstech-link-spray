package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.service.SprayService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/spray")
open class SprayApiController {

    @GetMapping
    fun getSprayLink(
        httpServletRequest: HttpServletRequest,
        @RequestParam @NotBlank inputLinkList: List<String>,
    ): ResponseEntity<String> =
        SprayService
            .getLinkSpray(httpServletRequest, inputLinkList)
            .let { ResponseEntity.ok(it) }
}
