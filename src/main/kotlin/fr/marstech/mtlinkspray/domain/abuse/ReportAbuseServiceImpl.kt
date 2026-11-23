package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.MailSenderService
import org.springframework.stereotype.Service

@Service
class ReportAbuseServiceImpl(
    private val abuseReportStore: AbuseReportStore,
    private val mailSenderService: MailSenderService
) : ReportAbuseService {

    val mailSubject: String = " MarsTech Link-Spray - Abuse report"
    val mailBodyPrefix: String = "New abuse report received:\n\n"

    override fun reportAbuse(
        inputAbuseDecsription: String,
        client: ClientContext,
    ) = abuseReportStore.create(
        AbuseReport(
            description = "$mailBodyPrefix$inputAbuseDecsription",
            authorIpAddress = client.ipAddress,
        )
    ).let {
        mailSenderService.sendMail(
            subject = mailSubject,
            body = it.toString()
        )
    }
}
