package fr.marstech.mtlinkspray.dto

import java.time.LocalTime

enum class SleepMode { WAKE_AT, SLEEP_AT }

data class SleepCycleOption(
    val time: LocalTime,
    val cycles: Int,
    val sleepMinutes: Long
)

data class SleepCycleResponse(
    val mode: SleepMode,
    val reference: LocalTime,
    val options: List<SleepCycleOption>
)
