package fr.marstech.mtlinkspray.service

import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * First path segments a generated short ID must never take (MLS-179): a short link equal to a
 * route such as "spray" or "paste" would be shadowed by that route. Read from the controller
 * mappings on first use, so a new controller is covered without editing a list, plus the
 * non-controller paths of `mt.link-spray.random-id.reserved` (static assets, actuator, error).
 */
@Component
class ReservedPaths(
    @Qualifier("requestMappingHandlerMapping") private val handlerMapping: ObjectProvider<RequestMappingHandlerMapping>,
    @Value($$"${mt.link-spray.random-id.reserved}") private val extraPaths: Set<String>,
) {

    val segments: Set<String> by lazy {
        handlerMapping.getObject().handlerMethods.keys
            .flatMap { it.patternValues }
            .map { it.removePrefix("/").substringBefore("/") }
            .filter { it.isNotEmpty() && !it.startsWith("{") }
            .toSet() + extraPaths
    }

    operator fun contains(id: String): Boolean = id in segments
}
