package fr.marstech.mtlinkspray.adapter.outbound.mongo.shared

import fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse.AbuseReportEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard.DashboardEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.image.ImageEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.paste.PasteEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shortener.LinkItem
import org.bson.Document
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.test.assertEquals

/**
 * Documents written by 2025 releases carry `author: null` and no `metadata` / `historyItems`
 * (MLS-282: `/wZonC` answered 500). Every [StandardEntity] must still read them.
 */
class OldDocumentReadTest {

    private val conversions = MongoCustomConversions(emptyList<Any>())
    private val converter = MappingMongoConverter(
        NoOpDbRefResolver.INSTANCE,
        MongoMappingContext().apply {
            setSimpleTypeHolder(conversions.simpleTypeHolder)
            afterPropertiesSet()
        },
    ).apply {
        setCustomConversions(conversions)
        afterPropertiesSet()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("entities")
    fun shouldReadDocumentWithNullAuthor(type: Class<out StandardEntity>, fields: Map<String, Any>) {
        val document = Document(
            mapOf(
                "_id" to "wZonC",
                "creationDate" to Date.from(CREATED.atZone(ZoneId.systemDefault()).toInstant()),
                "isEnabled" to true,
                "author" to null,
            ) + fields
        )

        val entity = converter.read(type, document)

        assertEquals("wZonC", entity.id)
        assertEquals(HistoryItem(dateTime = CREATED), entity.author)
        assertEquals(emptyList(), entity.historyItems)
        assertEquals(emptyMap(), entity.metadata)
    }

    companion object {
        private val CREATED = LocalDateTime.of(2025, 9, 18, 13, 30, 26, 327_000_000)

        @JvmStatic
        fun entities(): List<Arguments> = listOf(
            Arguments.of(LinkItem::class.java, mapOf("target" to Document("targetUrl", "https://example.com"))),
            Arguments.of(PasteEntity::class.java, mapOf("content" to "hello")),
            Arguments.of(DashboardEntity::class.java, emptyMap<String, Any>()),
            Arguments.of(
                ImageEntity::class.java,
                mapOf("apiKeyId" to "k", "s3Key" to "images/a.png", "originalFilename" to "a.png", "contentType" to "image/png", "sizeBytes" to 1L),
            ),
            Arguments.of(AbuseReportEntity::class.java, emptyMap<String, Any>()),
        )
    }
}
