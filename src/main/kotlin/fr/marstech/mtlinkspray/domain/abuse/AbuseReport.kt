package fr.marstech.mtlinkspray.domain.abuse

import java.time.LocalDateTime
import java.util.UUID

data class AbuseReport(
    val id: String = UUID.randomUUID().toString(),
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val description: String,
    val authorIpAddress: String?,
)
