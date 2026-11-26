package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.MailPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class ReportAbuseServiceTest {
    private val abuseReportStore = mock<AbuseReportStore>()
    private val mailPort = mock<MailPort>()
    private val reportAbuseService = ReportAbuseService(abuseReportStore, mailPort)

    @Test
    fun reportAbuse() {
        // Given
        whenever(abuseReportStore.create(any())).thenAnswer { it.arguments[0] as AbuseReport }

        // When
        reportAbuseService.reportAbuse("spam-xyz", ClientContext(ipAddress = "127.0.0.1"))

        // Then: stored with the caller's raw description and IP, then mailed as readable lines
        val report = argumentCaptor<AbuseReport>().also { verify(abuseReportStore).create(it.capture()) }.firstValue
        assertEquals("spam-xyz", report.description)
        assertEquals("127.0.0.1", report.authorIpAddress)
        val body = argumentCaptor<String>().also { verify(mailPort).sendMail(eq("MarsTech Link-Spray - Abuse report"), it.capture()) }.firstValue
        assertEquals(reportAbuseService.mailBody(report), body)
        assertFalse(body.contains("AbuseReport("), body)
    }

    @Test
    fun shouldFormatMailBodyWithZoneAndRawDescription() {
        // Given: a multi-line description whose second line starts with "|"
        val report = AbuseReport(
            id = "6d89e225",
            creationDate = LocalDateTime.of(2025, 2, 21, 16, 29, 46, 572_392_000),
            description = "Plop t'as vu ?\n  | kept",
            authorIpAddress = null,
        )

        // When
        val body = reportAbuseService.mailBody(report)

        // Then
        val zone = ZoneId.systemDefault()
        val expectedDate = report.creationDate.atZone(zone).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (VV)"))
        assertEquals(
            "New abuse report received.\n\nReport ID: 6d89e225\nDate: $expectedDate\nReporter IP: unknown\n\n" +
                "Description:\nPlop t'as vu ?\n  | kept",
            body,
        )
        // Pinned shape, independent of the production pattern: "2025-02-21 16:29:46 <zone name> (<zone id>)"
        assertTrue(Regex("""2025-02-21 16:29:46 \S+ \(\Q${zone.id}\E\)""").matches(expectedDate), expectedDate)
    }
}
