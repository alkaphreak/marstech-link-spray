package fr.marstech.mtlinkspray.enums

import java.time.Duration
import java.time.Period
import java.time.temporal.TemporalAmount

enum class ExpirationEnum(
    val expiration: String,
    val temporalAmount: TemporalAmount
) {
    NEVER(expiration = "NEVER", temporalAmount = Period.ofYears(100)),
    ONE_HOUR("ONE_HOUR", Duration.ofHours(1)),
    ONE_DAY("ONE_DAY", Duration.ofDays(1)),
    ONE_WEEK("ONE_WEEK", Period.ofWeeks(1)),
    ONE_MONTH("ONE_MONTH", Period.ofMonths(1)),
    ONE_YEAR("ONE_YEAR", Period.ofYears(1));

    companion object {
        fun fromExpiration(expiration: String): ExpirationEnum? =
            entries.firstOrNull { it.expiration == expiration }
    }
}
