package fr.marstech.mtlinkspray.utils

import fr.marstech.mtlinkspray.dto.SleepMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalTime

class SleepCyclesTest {

    @Test
    fun shouldListBedtimesFromLongestNightWrappingMidnight() {
        // Given
        val wakeAt = LocalTime.of(7, 0)
        // When
        val result = SleepCycles.bedtimes(wakeAt)
        // Then
        assertEquals(SleepMode.WAKE_AT, result.mode)
        assertEquals(listOf("21:45", "23:15", "00:45"), result.options.map { it.time.toString() })
        assertEquals(listOf(6, 5, 4), result.options.map { it.cycles })
        assertEquals(listOf(540L, 450L, 360L), result.options.map { it.sleepMinutes })
    }

    @Test
    fun shouldListWakeTimes() {
        // Given
        val sleepAt = LocalTime.of(23, 0)
        // When
        val result = SleepCycles.wakeTimes(sleepAt)
        // Then
        assertEquals(SleepMode.SLEEP_AT, result.mode)
        assertEquals(listOf("08:15", "06:45", "05:15"), result.options.map { it.time.toString() })
    }
}
