package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.MailPort
import org.springframework.stereotype.Service

@Service
class ReportAbuseService(
    private val abuseReportStore: AbuseReportStore,
    private val mailPort: MailPort
) {

    val mailSubject: String = " MarsTech Link-Spray - Abuse report"
    val mailBodyPrefix: String = "New abuse report received:\n\n"

    fun reportAbuse(
        inputAbuseDecsription: String,
        client: ClientContext,
    ) = abuseReportStore.create(
        AbuseReport(
            description = "$mailBodyPrefix$inputAbuseDecsription",
            authorIpAddress = client.ipAddress,
        )
    ).let {
        mailPort.sendMail(
            subject = mailSubject,
            body = it.toString()
        )
    }
}
