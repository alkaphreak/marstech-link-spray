package fr.marstech.mtlinkspray.domain.cron

import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

/** Cron expressions (Spring 6-field syntax, seconds first, MLS-220) and the in-memory scheduler, in UTC. */
interface CronSchedulerPort {
    /** The first fire time after [after], null when the expression never fires again. Throws [IllegalArgumentException] on an invalid expression. */
    fun next(expression: String, after: LocalDateTime): LocalDateTime?

    fun schedule(expression: String, task: Runnable): ScheduledFuture<*>?

    /** Stops the scheduler; idempotent. */
    fun shutdown()
}
