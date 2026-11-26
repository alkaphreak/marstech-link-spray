package fr.marstech.mtlinkspray.domain.abuse
import fr.marstech.mtlinkspray.domain.shared.ClientContext
import fr.marstech.mtlinkspray.domain.shared.MailPort
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class ReportAbuseService(
    private val abuseReportStore: AbuseReportStore,
    private val mailPort: MailPort,
) {

    val mailSubject: String = "MarsTech Link-Spray - Abuse report"

    fun reportAbuse(
        inputAbuseDecsription: String,
        client: ClientContext,
    ) = abuseReportStore.create(
        AbuseReport(
            description = inputAbuseDecsription,
            authorIpAddress = client.ipAddress,
        )
    ).let {
        mailPort.sendMail(
            subject = mailSubject,
            body = mailBody(it)
        )
    }

    // creationDate is a LocalDateTime in the server's zone: the zone is printed so the reader knows which clock it is.
    // Joined, not trimMargin: a description line starting with "|" must reach the mail untouched.
    fun mailBody(report: AbuseReport): String = listOf(
        "New abuse report received.",
        "",
        "Report ID: ${report.id}",
        "Date: ${report.creationDate.atZone(ZoneId.systemDefault()).format(DATE_FORMAT)}",
        "Reporter IP: ${report.authorIpAddress ?: "unknown"}",
        "",
        "Description:",
        report.description,
    ).joinToString("\n")

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z (VV)")
    }
}
