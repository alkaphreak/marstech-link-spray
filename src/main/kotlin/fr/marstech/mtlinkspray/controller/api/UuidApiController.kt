package fr.marstech.mtlinkspray.controller.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/uuid")
class UuidApiController {

    @GetMapping()
    fun getUuid(): String = UUID.randomUUID().toString()
}
