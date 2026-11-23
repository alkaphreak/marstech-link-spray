package fr.marstech.mtlinkspray.adapter.outbound.mongo.abuse

import org.springframework.data.annotation.TypeAlias
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.HistoryItem
import fr.marstech.mtlinkspray.adapter.outbound.mongo.shared.StandardEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "mt-link-spray-abuse-reports")
@TypeAlias("fr.marstech.mtlinkspray.entity.AbuseReportEntity")
data class AbuseReportEntity(
    @Id override val id: String = UUID.randomUUID().toString(),
    override val creationDate: LocalDateTime = LocalDateTime.now(),
    override val expiresAt: LocalDateTime? = null,
    override var isEnabled: Boolean = true,
    override var description: String? = null,
    override var metadata: MutableMap<String, String> = mutableMapOf(),
    override var author: HistoryItem,
    override var historyItems: MutableList<HistoryItem> = mutableListOf(),
) : StandardEntity
