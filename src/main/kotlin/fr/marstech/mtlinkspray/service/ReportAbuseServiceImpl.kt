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
    private val abuseReportRepository: AbuseReportRepository, private val mailSenderService: MailSenderService
) : ReportAbuseService {

    override fun reportAbuse(
        inputAbuseDecsription: String, httpServletRequest: HttpServletRequest
    ) = "reportAbuse".let { it ->
        AbuseReportEntity(
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            null,
            true,
            "$it:$inputAbuseDecsription",
            Map.of<String, String>(),
            HistoryItem(
                httpServletRequest.remoteAddr, LocalDateTime.now(), it
            )
        ).let { abuseReportRepository.save(it) }
            .let { mailSenderService.sendMail(" MarsTech Link-Spray - Abuse report", it.toString()) }

    }
}
