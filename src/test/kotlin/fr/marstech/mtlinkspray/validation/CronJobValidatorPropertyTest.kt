package fr.marstech.mtlinkspray.validation

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import org.junit.jupiter.api.Assertions.assertFalse
import java.net.InetAddress

class CronJobValidatorPropertyTest {

    @Property(seed = "220")
    fun shouldNeverClassifyTenSlashEightAsPublic(@ForAll @IntRange(min = 0, max = 255) b: Int, @ForAll @IntRange(min = 0, max = 255) c: Int) {
        assertFalse(CronJobValidator.isPublic(InetAddress.getByName("10.$b.$c.1")))
    }

    @Property(seed = "220")
    fun shouldNeverClassifyCgnatAsPublic(@ForAll @IntRange(min = 64, max = 127) b: Int, @ForAll @IntRange(min = 0, max = 255) c: Int) {
        assertFalse(CronJobValidator.isPublic(InetAddress.getByName("100.$b.$c.1")))
    }

    @Property(seed = "220")
    fun shouldRejectEverySubMinuteSecondsStep(@ForAll @IntRange(min = 1, max = 59) step: Int) {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> { CronJobValidator.validateSchedule("*/$step * * * * *") }
    }
}
