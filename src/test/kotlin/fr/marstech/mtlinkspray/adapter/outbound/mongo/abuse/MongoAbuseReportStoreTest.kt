package fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse

import fr.marstech.mtlinkspray.domain.abuse.AbuseReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class MongoAbuseReportStoreTest {
    private val repository = mock<AbuseReportRepository>()
    private val store = MongoAbuseReportStore(repository)

    @Test
    fun shouldInsertReportWithAuthorIpAndAction() {
        // Given
        val report = AbuseReport(description = "spam-xyz", authorIpAddress = "203.0.113.7")

        // When
        val stored = store.create(report)

        // Then
        val entity = argumentCaptor<AbuseReportEntity>().also { verify(repository).insert(it.capture()) }.firstValue
        assertEquals(report.id, entity.id)
        assertEquals(report.creationDate, entity.creationDate)
        assertEquals(report.description, entity.description)
        assertEquals("203.0.113.7", entity.author.ipAddress)
        assertEquals("REPORT_ABUSE", entity.author.action)
        assertEquals(report, stored)
    }
}
