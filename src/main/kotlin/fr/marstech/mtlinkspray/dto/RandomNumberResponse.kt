package fr.marstech.mtlinkspray.dto

import java.time.Instant

data class RandomNumberResponse(
    val value: Int,
    val min: Int,
    val max: Int,
    val timestamp: Instant
)
