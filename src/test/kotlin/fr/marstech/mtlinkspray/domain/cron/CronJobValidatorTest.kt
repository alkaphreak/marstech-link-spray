package fr.marstech.mtlinkspray.domain.cron
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import fr.marstech.mtlinkspray.adapter.outbound.scheduler.SpringCronScheduler
import java.net.InetAddress

class CronJobValidatorTest {

    private val cron = SpringCronScheduler()

    private fun resolvingTo(ip: String): (String) -> Array<InetAddress> = { arrayOf(InetAddress.getByName(ip)) }

    @Test
    fun shouldAcceptFiveMinuteSchedule() {
        // Given / When
        // Then
        assertDoesNotThrow { CronJobValidator.validateSchedule("0 */5 * * * *", cron) }
    }

    @Test
    fun shouldRejectInvalidCronExpression() {
        assertThrows<IllegalArgumentException> { CronJobValidator.validateSchedule("not a cron", cron) }
    }

    @Test
    fun shouldRejectScheduleFiringMoreThanOncePerMinute() {
        assertThrows<IllegalArgumentException> { CronJobValidator.validateSchedule("*/30 * * * * *", cron) }
    }

    @Test
    fun shouldRejectScheduleWithShortGapInsideTheHour() {
        // Given: seconds 0 to 5 of minute 0, six runs one second apart
        assertThrows<IllegalArgumentException> { CronJobValidator.validateSchedule("0-5 0 * * * *", cron) }
    }

    @Test
    fun shouldAcceptPublicHttpsUrl() {
        val uri = CronJobValidator.validateUrl("https://example.com/health", resolvingTo("93.184.215.14"))
        assertEquals("example.com", uri.host)
    }

    @Test
    fun shouldRejectNonHttpScheme() {
        assertThrows<IllegalArgumentException> { CronJobValidator.validateUrl("ftp://example.com", resolvingTo("93.184.215.14")) }
    }

    @Test
    fun shouldRejectUrlWithCredentials() {
        assertThrows<IllegalArgumentException> { CronJobValidator.validateUrl("https://u:p@example.com", resolvingTo("93.184.215.14")) }
    }

    @Test
    fun shouldRejectUnresolvableHost() {
        assertThrows<IllegalArgumentException> {
            CronJobValidator.validateUrl("https://nowhere.invalid") { throw java.net.UnknownHostException(it) }
        }
    }

    @Test
    fun shouldRejectHostResolvingToAnyPrivateAddress() {
        assertThrows<IllegalArgumentException> {
            CronJobValidator.validateUrl("https://mixed.example") { arrayOf(InetAddress.getByName("93.184.215.14"), InetAddress.getByName("10.0.0.1")) }
        }
    }

    @Test
    fun shouldClassifyReservedAddressesAsNotPublic() {
        listOf("127.0.0.1", "0.0.0.0", "10.1.2.3", "172.16.0.1", "192.168.1.10", "169.254.169.254", "100.64.0.1",
            "100.127.255.254", "224.0.0.1", "192.0.0.8", "198.18.0.1", "198.19.255.255", "240.0.0.1", "255.255.255.255",
            "::1", "fe80::1", "fd00::1", "fc00::1", "::ffff:10.0.0.1", "64:ff9b::a00:1", "::127.0.0.1", "2002:a00:1::1")
            .forEach { assertFalse(CronJobValidator.isPublic(InetAddress.getByName(it)), it) }
    }

    @Test
    fun shouldClassifyPublicAddressesAsPublic() {
        listOf("8.8.8.8", "1.1.1.1", "100.63.255.255", "100.128.0.1", "198.20.0.1", "192.0.3.1", "2606:4700:4700::1111")
            .forEach { assertTrue(CronJobValidator.isPublic(InetAddress.getByName(it)), it) }
    }
}
