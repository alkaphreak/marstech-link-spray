package fr.marstech.mtlinkspray.domain.devtools
import java.time.Instant

data class RandomNumber(
    val value: Int,
    val min: Int,
    val max: Int,
    val timestamp: Instant
)
