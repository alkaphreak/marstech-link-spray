package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.IpInfoResponse
import fr.marstech.mtlinkspray.dto.IpLookupRequest
import fr.marstech.mtlinkspray.service.IpService
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Nothing is stored or logged: the address is looked up and returned. */
@RestController
@RequestMapping("/api/ip")
class IpApiController(
    private val ipService: IpService
) {

    @GetMapping
    fun lookup(@Valid request: IpLookupRequest, httpServletRequest: HttpServletRequest): IpInfoResponse =
        ipService.lookup(request.ip ?: NetworkUtils.getIpAddress(httpServletRequest).orEmpty())
}
