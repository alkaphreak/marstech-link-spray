package fr.marstech.mtlinkspray.conf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import kotlin.time.Duration.Companion.milliseconds

@Configuration
@Profile("dev")
class ApplicationReadyEventHandlerForDev(
    private val environment: Environment,
    @param:Value($$"${mt.link-spray.version:unknown}") private val mtLinkSprayVersion: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @EventListener(ApplicationReadyEvent::class)
    fun displayServerUrlInConsole() = scope.launch {
        kotlinx.coroutines.delay(1000.milliseconds)
        displayLocalServerInfo()
    }

    private fun displayLocalServerInfo() {
        log.info("Local server : http://localhost:${environment.getProperty("server.port")}")
        log.info("App version : $mtLinkSprayVersion")
    }

    companion object Companion {
        val log: Logger = LoggerFactory.getLogger(ApplicationReadyEventHandlerForDev::class.java)
    }
}
