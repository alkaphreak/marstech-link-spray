package fr.marstech.mtlinkspray.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class RateLimitPropertiesTest {

    @Test
    fun shouldKeepTheOtherDefaultsWhenOneKeyIsSet() {
        // Given: only the windows are set, as in the test profiles
        val source = MapConfigurationPropertySource(
            mapOf("mt.link-spray.rate-limit.writes.window-ms" to "0", "mt.link-spray.rate-limit.abuse.window-ms" to "0"),
        )

        // When
        val p = Binder(source).bind("mt.link-spray.rate-limit", RateLimitProperties::class.java).get()

        // Then
        assertEquals(RateLimitProperties.Writes(200, 0), p.writes)
        assertEquals(RateLimitProperties.Abuse(5, 0), p.abuse)
    }
}
