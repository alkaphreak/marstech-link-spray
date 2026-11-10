package fr.marstech.mtlinkspray.utils

import fr.marstech.mtlinkspray.dto.SleepCycleOption
import fr.marstech.mtlinkspray.dto.SleepCycleResponse
import fr.marstech.mtlinkspray.dto.SleepMode
import java.time.LocalTime

/**
 * Sleep cycle arithmetic (MLS-213): 90-minute cycles, 15 minutes to fall asleep, 4 to 6 cycles a
 * night. Times wrap around midnight. Options are listed from the longest night to the shortest.
 */
object SleepCycles {

    const val CYCLE_MINUTES = 90L
    const val FALL_ASLEEP_MINUTES = 15L
    private val CYCLE_COUNTS = 6 downTo 4

    /** Bedtimes for a wake-up time. */
    fun bedtimes(wakeAt: LocalTime): SleepCycleResponse =
        SleepCycleResponse(SleepMode.WAKE_AT, wakeAt, CYCLE_COUNTS.map {
            option(wakeAt.minusMinutes(inBedMinutes(it)), it)
        })

    /** Wake-up times for a bedtime. */
    fun wakeTimes(sleepAt: LocalTime): SleepCycleResponse =
        SleepCycleResponse(SleepMode.SLEEP_AT, sleepAt, CYCLE_COUNTS.map {
            option(sleepAt.plusMinutes(inBedMinutes(it)), it)
        })

    private fun inBedMinutes(cycles: Int) = FALL_ASLEEP_MINUTES + cycles * CYCLE_MINUTES

    private fun option(time: LocalTime, cycles: Int) = SleepCycleOption(time, cycles, cycles * CYCLE_MINUTES)
}
