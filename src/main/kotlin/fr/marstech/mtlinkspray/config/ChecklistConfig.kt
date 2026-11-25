package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.domain.checklist.ChecklistLimits
import fr.marstech.mtlinkspray.domain.checklist.ChecklistService
import fr.marstech.mtlinkspray.domain.checklist.ChecklistStore
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties("mt.link-spray.checklist")
data class ChecklistProperties(
    val maxTemplatesPerNamespace: Int = 50,
    val maxInstancesPerNamespace: Int = 500,
    val maxItems: Int = 200,
)

/** The checklist service with its limits, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ChecklistProperties::class)
class ChecklistConfig {

    @Bean
    fun checklistService(store: ChecklistStore, apiKeyResolver: ApiKeyResolver, p: ChecklistProperties) =
        ChecklistService(store, apiKeyResolver, ChecklistLimits(p.maxTemplatesPerNamespace, p.maxInstancesPerNamespace, p.maxItems))
}
