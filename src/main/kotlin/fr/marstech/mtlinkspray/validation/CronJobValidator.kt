package fr.marstech.mtlinkspray.validation

import org.springframework.scheduling.support.CronExpression
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime

/** Rules a cron job must pass before it is stored or run (MLS-220). Failures throw [IllegalArgumentException] (400). */
object CronJobValidator {
    val MIN_INTERVAL: Duration = Duration.ofMinutes(1)

    // ponytail: checks the next 10 gaps only; a schedule whose short gap comes later slips through, cap is harmless (1 req/s worst case)
    private const val CHECKED_GAPS = 10

    fun validateSchedule(schedule: String, from: LocalDateTime = LocalDateTime.now()): CronExpression {
        val cron = try {
            CronExpression.parse(schedule)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid cron expression '$schedule': ${e.message}")
        }
        var previous = cron.next(from) ?: throw IllegalArgumentException("Cron expression '$schedule' never fires")
        repeat(CHECKED_GAPS) {
            val next = cron.next(previous) ?: return cron
            require(Duration.between(previous, next) >= MIN_INTERVAL) {
                "Cron expression '$schedule' fires more often than once per ${MIN_INTERVAL.toMinutes()} minute"
            }
            previous = next
        }
        return cron
    }

    /** Rejects anything but a public http(s) target, so a job cannot probe the server or private networks (SSRF). */
    fun validateUrl(url: String, resolve: (String) -> Array<InetAddress> = InetAddress::getAllByName): URI {
        val uri = try {
            URI(url)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL '$url'")
        }
        require(uri.scheme?.lowercase() in setOf("http", "https")) { "URL must use http or https" }
        require(uri.rawUserInfo == null) { "URL must not carry credentials" }
        val host = uri.host ?: throw IllegalArgumentException("URL has no host")
        val addresses = try {
            resolve(host)
        } catch (e: Exception) {
            throw IllegalArgumentException("Host '$host' does not resolve")
        }
        require(addresses.isNotEmpty() && addresses.all(::isPublic)) { "URL must point to a public address" }
        return uri
    }

    fun isPublic(address: InetAddress): Boolean {
        if (address.isAnyLocalAddress || address.isLoopbackAddress || address.isLinkLocalAddress ||
            address.isSiteLocalAddress || address.isMulticastAddress
        ) return false
        val b = address.address.map { it.toInt() and 0xFF }
        return when (address) {
            // 0/8, 100.64/10 (CGNAT, Tailscale), 192.0.0/24, 198.18/15 (benchmark), 240/4 (reserved, broadcast)
            is Inet4Address -> b[0] != 0 && !(b[0] == 100 && b[1] in 64..127) && !(b[0] == 192 && b[1] == 0 && b[2] == 0) &&
                !(b[0] == 198 && b[1] in 18..19) && b[0] < 240
            // fc00::/7 unique local; 64:ff9b::/96 (NAT64) and 2002::/16 (6to4) can embed a private IPv4
            // ::/96 IPv4-compatible (::127.0.0.1) stays an Inet6Address
            is Inet6Address -> (b[0] and 0xFE) != 0xFC && b.subList(0, 12).any { it != 0 } && !(b[0] == 0x20 && b[1] == 0x02) &&
                !(b.subList(0, 12) == listOf(0, 0x64, 0xff, 0x9b, 0, 0, 0, 0, 0, 0, 0, 0))
            else -> false
        }
    }
}
