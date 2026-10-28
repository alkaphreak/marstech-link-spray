package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.DnsLookupRequest
import fr.marstech.mtlinkspray.dto.DnsLookupResponse
import fr.marstech.mtlinkspray.dto.DnsPropagationResponse
import fr.marstech.mtlinkspray.dto.DnsReverseRequest
import fr.marstech.mtlinkspray.service.DnsService
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
