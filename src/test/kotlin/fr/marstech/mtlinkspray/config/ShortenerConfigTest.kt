package fr.marstech.mtlinkspray.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import kotlin.test.assertEquals

/** The former `@Value` keys, `cache.treshold` spelling and empty `prefix` included; none has a default. */
class ShortenerConfigTest {

    private fun bind(properties: Map<String, String>): RandomIdProperties =
        Binder(MapConfigurationPropertySource(properties)).bindOrCreate("mt.link-spray.random-id", RandomIdProperties::class.java)

    private val keys = mapOf(
        "mt.link-spray.random-id.charset" to "abc",
        "mt.link-spray.random-id.length" to "5",
        "mt.link-spray.random-id.prefix" to "",
        "mt.link-spray.random-id.cache.enabled" to "true",
        "mt.link-spray.random-id.cache.depth" to "100",
        "mt.link-spray.random-id.cache.treshold" to "10",
    )

    @Test
    fun applicationKeysBind() {
        assertEquals(RandomIdProperties(5, "", "abc", RandomIdProperties.Cache(true, 100, 10)), bind(keys))
    }

    @Test
    fun missingKeyFailsAsTheValueAnnotationDid() {
        assertThrows<BindException> { bind(keys - "mt.link-spray.random-id.length") }
    }

    @Test
    fun urlReputationIsOffWithoutAKeyAndUrlhausWithOne() {
        kotlin.test.assertFalse(ShortenerConfig().urlReputation("").isMalicious("https://evil.example"))
        kotlin.test.assertIs<fr.marstech.mtlinkspray.adapter.outbound.urlhaus.UrlhausReputation>(ShortenerConfig().urlReputation("key"))
    }
}
