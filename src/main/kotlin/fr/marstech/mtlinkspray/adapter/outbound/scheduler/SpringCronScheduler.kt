package fr.marstech.mtlinkspray.adapter.outbound.scheduler
import fr.marstech.mtlinkspray.domain.cron.CronSchedulerPort
import jakarta.annotation.PreDestroy
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronExpression
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ScheduledFuture

/** Spring `CronExpression` parsing (6 fields, seconds first) and a private [ThreadPoolTaskScheduler], in UTC. */
@Component
class SpringCronScheduler : CronSchedulerPort {

    // Not a bean: an Executor bean would switch off Boot's applicationTaskExecutor.
    // ponytail: one shared pool, each run is capped by its timeout (10 s max); per-namespace limits if namespaces grow
    private val scheduler = ThreadPoolTaskScheduler().apply {
        poolSize = 4
        setThreadNamePrefix("cron-")
        initialize()
    }

    @PreDestroy
    override fun shutdown() = scheduler.shutdown()

    override fun next(expression: String, after: LocalDateTime): LocalDateTime? = CronExpression.parse(expression).next(after)

    override fun schedule(expression: String, task: Runnable): ScheduledFuture<*>? =
        scheduler.schedule(task, CronTrigger(expression, ZoneOffset.UTC))
}
