package fr.marstech.mtlinkspray.config

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import kotlin.test.assertEquals

/** Pins the defaults the former `@Value` parameters carried: no other test sets the unconfigured `img.*` keys. */
class NativeToolsConfigTest {

    private inline fun <reified T : Any> bind(prefix: String, properties: Map<String, String> = emptyMap()): T =
        Binder(MapConfigurationPropertySource(properties)).bindOrCreate(prefix, T::class.java)

    @Test
    fun defaultsMatchTheFormerValueAnnotations() {
        assertEquals(
            OcrProperties("tesseract", 10_485_760, 30, 10, 3_600_000, 1, 20_000_000, 12_000_000),
            bind("mt.link-spray.ocr"),
        )
        assertEquals(
            ImageCompressionProperties("cwebp", "caesiumclt", 10_485_760, 30, 8_000_000, 8_000_000, 3_000_000, 20, 3_600_000),
            bind("mt.link-spray.img"),
        )
    }

    @Test
    fun kebabCaseKeysBind() {
        val img = bind<ImageCompressionProperties>(
            "mt.link-spray.img",
            mapOf("mt.link-spray.img.anonymous-window-ms" to "5", "mt.link-spray.img.caesium-command" to "/bin/c"),
        )
        assertEquals(5, img.anonymousWindowMs)
        assertEquals("/bin/c", img.caesiumCommand)
    }
}
