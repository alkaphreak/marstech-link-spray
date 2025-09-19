package fr.marstech.mtlinkspray.controller.api

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ApiRootController(
    @Value("\${mt.link-spray.version}") val version: String
) {
    @GetMapping("/version")
    fun version(): String = version
}