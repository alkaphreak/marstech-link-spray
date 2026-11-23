package fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse

import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.domain.abuse.AbuseReport
import fr.marstech.mtlinkspray.domain.abuse.AbuseReportStore
import org.springframework.stereotype.Component

@Component
class MongoAbuseReportStore(
    private val repository: AbuseReportRepository,
) : AbuseReportStore {

    override fun create(report: AbuseReport): AbuseReport {
        repository.insert(
            AbuseReportEntity(
                id = report.id,
                creationDate = report.creationDate,
                description = report.description,
                author = HistoryItem(ipAddress = report.authorIpAddress, action = "REPORT_ABUSE"),
            )
        )
        return report
    }
}
