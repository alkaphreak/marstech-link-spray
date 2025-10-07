package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.AbuseReportEntity
import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.repository.AbuseReportRepository
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime.now
import java.util.*

@Service
class ReportAbuseServiceImpl(
    private val abuseReportRepository: AbuseReportRepository,
    private val mailSenderService: MailSenderService
) : ReportAbuseService {

    val action: String = "REPORT_ABUSE"
    val mailSubject: String = " MarsTech Link-Spray - Abuse report"
    val mailBodyPrefix: String = "New abuse report received:\n\n"

    override fun reportAbuse(
        inputAbuseDecsription: String,
        httpServletRequest: HttpServletRequest
    ) = abuseReportRepository.save(
        AbuseReportEntity(
            id = UUID.randomUUID().toString(),
            creationDate = now(),
            description = "$mailBodyPrefix$inputAbuseDecsription",
            author = HistoryItem(
                ipAddress = NetworkUtils.getIpAddress(httpServletRequest),
                action = action,
            )
        )
    ).let {
        mailSenderService.sendMail(
            subject = mailSubject,
            body = it.toString()
        )
    }
}
