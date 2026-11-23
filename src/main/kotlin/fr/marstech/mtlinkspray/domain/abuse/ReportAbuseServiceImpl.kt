package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.MailSenderService
import fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse.AbuseReportEntity
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse.AbuseReportRepository
import fr.marstech.mtlinkspray.adapter.inbound.web.common.NetworkUtils
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
