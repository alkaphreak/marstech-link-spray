package fr.marstech.mtlinkspray.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.util.Pair
import java.time.LocalDateTime
import java.util.*

@Document(collection = "mt-link-spray-items")
data class LinkItem(
    @Id override val id: String = UUID.randomUUID().toString(),
    override val creationDate: LocalDateTime = LocalDateTime.now(),
    override val expiresAt: LocalDateTime? = null,
    override var isEnabled: Boolean = true,
    override var description: String? = null,
    override var metadata: MutableMap<String, String> = mutableMapOf(),
    override var author: HistoryItem,
    override var historyItems: MutableList<HistoryItem> = mutableListOf(),
    val target: LinkItemTarget,
) : StandardEntity