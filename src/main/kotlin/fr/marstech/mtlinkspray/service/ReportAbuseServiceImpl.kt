package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.AbuseReportEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.AbuseReportRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import java.util.Map

@Service
class ReportAbuseServiceImpl(
    private val abuseReportRepository: AbuseReportRepository,
    private val mailSenderService: MailSenderService
) : ReportAbuseService {

    override fun reportAbuse(
        inputAbuseDecsription: String, httpServletRequest: HttpServletRequest
    ) = "reportAbuse".let { it ->
        AbuseReportEntity(
            id = UUID.randomUUID().toString(),
            creationDate = LocalDateTime.now(),
            expiresAt = null,
            isEnabled = true,
            description = "$it:$inputAbuseDecsription",
            metadata = Map.of<String, String>(),
            author = HistoryItem(
                ipAddress = httpServletRequest.remoteAddr,
                dateTime = LocalDateTime.now(),
                action = it
            )
        ).let { abuseReportRepository.save(it) }
            .let {
                mailSenderService.sendMail(
                    subject = " MarsTech Link-Spray - Abuse report",
                    body = it.toString()
                )
            }

    }
}
