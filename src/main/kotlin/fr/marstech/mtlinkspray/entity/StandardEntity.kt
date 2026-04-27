package fr.marstech.mtlinkspray.entity

import java.time.LocalDateTime

/**
 * Abstract class representing a standard entity with common properties.
 * This class is used as a base class for other entities in the application.
 */
interface StandardEntity {
    val id: String
    val creationDate: LocalDateTime
    val expiresAt: LocalDateTime?
    var isEnabled: Boolean
    var description: String?
    var metadata: MutableMap<String, String>
    var author: HistoryItem
    var historyItems: MutableList<HistoryItem>
}
