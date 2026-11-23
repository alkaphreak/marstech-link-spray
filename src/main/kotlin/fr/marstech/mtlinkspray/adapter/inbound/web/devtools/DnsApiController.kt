package fr.marstech.mtlinkspray.adapter.inbound.web.devtools
import fr.marstech.mtlinkspray.domain.devtools.DnsService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/dns")
class DnsApiController(
    private val dnsService: DnsService
) {

    @GetMapping
    fun lookup(@Valid request: DnsLookupRequest): DnsLookupResponse =
        dnsService.lookup(request.name.orEmpty(), request.type)

    @GetMapping("/reverse")
    fun reverse(@Valid request: DnsReverseRequest): DnsLookupResponse =
        dnsService.reverse(request.ip.orEmpty())

    @GetMapping("/propagation")
    fun propagation(@Valid request: DnsLookupRequest): DnsPropagationResponse =
        dnsService.propagation(request.name.orEmpty(), request.type)
}
