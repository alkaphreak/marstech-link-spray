package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.MailSenderService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ReportAbuseServiceImplTest {
    private val abuseReportStore = mock<AbuseReportStore>()
    private val mailSenderService = mock<MailSenderService>()
    private val reportAbuseService = ReportAbuseServiceImpl(abuseReportStore, mailSenderService)

    @Test
    fun reportAbuse() {
        // Given
        whenever(abuseReportStore.create(any())).thenAnswer { it.arguments[0] as AbuseReport }

        // When
        reportAbuseService.reportAbuse("spam-xyz", ClientContext(ipAddress = "127.0.0.1"))

        // Then: stored with the caller's IP, then mailed
        val report = argumentCaptor<AbuseReport>().also { verify(abuseReportStore).create(it.capture()) }.firstValue
        assertEquals("New abuse report received:\n\nspam-xyz", report.description)
        assertEquals("127.0.0.1", report.authorIpAddress)
        val body = argumentCaptor<String>().also { verify(mailSenderService).sendMail(eq(" MarsTech Link-Spray - Abuse report"), it.capture()) }.firstValue
        assertEquals(report.toString(), body)
        assertTrue(body.contains("spam-xyz") && body.contains("127.0.0.1"), body)
    }
}
