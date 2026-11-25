package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.domain.devtools.IpInfo
import fr.marstech.mtlinkspray.domain.devtools.IpService
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils
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
    fun lookup(@Valid request: IpLookupRequest, httpServletRequest: HttpServletRequest): IpInfo =
        ipService.lookup(request.ip ?: NetworkUtils.getIpAddress(httpServletRequest).orEmpty())
}
