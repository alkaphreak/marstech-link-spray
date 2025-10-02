package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.AbuseReportEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.AbuseReportRepository
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class ReportAbuseServiceImplTest {
    private val abuseReportRepository = mock(AbuseReportRepository::class.java)
    private val mailSenderService = mock(MailSenderService::class.java)
    private val reportAbuseService = ReportAbuseServiceImpl(abuseReportRepository, mailSenderService)

    @BeforeEach
    fun setUp() {
        `when`(abuseReportRepository.save(org.mockito.kotlin.any())).thenAnswer { it.arguments[0] as AbuseReportEntity }
        `when`(abuseReportRepository.count()).thenReturn(1)
        `when`(abuseReportRepository.findAll()).thenReturn(
            listOf(
                AbuseReportEntity(
                    description = "reportAbuse:abuse", author = HistoryItem(
                        "dummy-author"
                    )
                )
            )
        )
    }

    @Test
    fun reportAbuse() {
        val request = mock<HttpServletRequest>()
        `when`(request.remoteAddr).thenReturn("127.0.0.1")

        val abuse = "abuse"
        reportAbuseService.reportAbuse(abuse, request)
        Assertions.assertEquals(1, abuseReportRepository.count())
        Assertions.assertEquals("reportAbuse:abuse", abuseReportRepository.findAll().first().description)
    }
}
