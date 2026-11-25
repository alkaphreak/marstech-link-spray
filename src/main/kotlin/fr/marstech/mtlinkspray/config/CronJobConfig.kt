package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.domain.cron.CronJobService
import fr.marstech.mtlinkspray.domain.cron.CronSchedulerPort
import fr.marstech.mtlinkspray.domain.cron.CronStore
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import fr.marstech.mtlinkspray.domain.shared.MailSenderService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import fr.marstech.mtlinkspray.domain.cron.WebhookClient

/** `mt.link-spray.cron.*` read by the service; `run-retention-days` is read by `MongoCronStore`. */
@ConfigurationProperties("mt.link-spray.cron")
data class CronProperties(val maxJobsPerNamespace: Int = 20)

/** The cron job service with its limit, started once the application is ready (ADR 0007). */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CronProperties::class)
class CronJobConfig {

    @Bean(destroyMethod = "stop")
    fun cronJobService(
        store: CronStore,
        cron: CronSchedulerPort,
        apiKeyResolver: ApiKeyResolver,
        mailSenderService: MailSenderService,
        webhook: WebhookClient,
        p: CronProperties,
    ) = CronJobService(store, cron, apiKeyResolver, mailSenderService, webhook, p.maxJobsPerNamespace)

    // A listener bean, not @EventListener: the service is a plain class built here.
    @Bean
    fun cronJobStartup(service: CronJobService) = ApplicationListener<ApplicationReadyEvent> { service.start() }
}
