package fr.marstech.mtlinkspray.utils

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalTime

/** For any time of day, going to bed at a suggested bedtime wakes you up at the requested time (MLS-213). */
class SleepCyclesPropertyTest {

    @Property(seed = "213")
    fun shouldRoundTripBedtimesAndWakeTimes(@ForAll @IntRange(min = 0, max = 1439) minuteOfDay: Int) {
        // Given
        val wakeAt = LocalTime.MIN.plusMinutes(minuteOfDay.toLong())
        // When
        val roundTrips = SleepCycles.bedtimes(wakeAt).options.map { bedtime ->
            SleepCycles.wakeTimes(bedtime.time).options.single { it.cycles == bedtime.cycles }.time
        }
        // Then
        roundTrips.forEach { assertEquals(wakeAt, it) }
    }
}
