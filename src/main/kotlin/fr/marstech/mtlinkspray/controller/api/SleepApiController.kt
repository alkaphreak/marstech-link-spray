package fr.marstech.mtlinkspray.controller.api

import fr.marstech.mtlinkspray.dto.SleepCycleResponse
import fr.marstech.mtlinkspray.utils.SleepCycles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

private val HH_MM: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)

/**
 * Sleep cycle calculator (MLS-213): exactly one of `wakeAt` or `sleepAt`, as HH:mm.
 * Times are parsed here with a strict HH:mm pattern (24:00 rejected), so a malformed value gets a readable 400 message.
 * Exactly one parameter must be present: a blank one still counts as present.
 */
@RestController
@RequestMapping("/api/sleep")
class SleepApiController {

    @GetMapping
    fun getSleep(
        @RequestParam(name = "wakeAt", required = false) wakeAt: String?,
        @RequestParam(name = "sleepAt", required = false) sleepAt: String?
    ): SleepCycleResponse {
        require((wakeAt == null) != (sleepAt == null)) {
            "Provide exactly one of wakeAt or sleepAt (HH:mm)"
        }
        return if (wakeAt != null) SleepCycles.bedtimes(parse(wakeAt))
        else SleepCycles.wakeTimes(parse(sleepAt!!))
    }

    private fun parse(time: String): LocalTime = try {
        LocalTime.parse(time, HH_MM)
    } catch (_: DateTimeParseException) {
        throw IllegalArgumentException("Time must be HH:mm, got '${time.take(10)}'")
    }
}
