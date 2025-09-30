package fr.marstech.mtlinkspray.controller.api

import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController(value = "rootApiController")
@RequestMapping("/api")
class RootApiController(
    @param:Value($$"${mt.link-spray.version}") @param:NotBlank val version: String
) {
    @GetMapping("/version")
    fun version(): String = version
}