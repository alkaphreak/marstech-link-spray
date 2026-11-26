package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.adapter.outbound.urlhaus.UrlhausReputation
import fr.marstech.mtlinkspray.domain.shortener.UrlReputationPort
import fr.marstech.mtlinkspray.domain.shortener.RandomIdGeneratorService
import fr.marstech.mtlinkspray.domain.shortener.RandomIdSettings
import fr.marstech.mtlinkspray.domain.shortener.ReservedPathsPort
import fr.marstech.mtlinkspray.domain.shortener.ShortLinkStore
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * `mt.link-spray.random-id.*`: no defaults, every key is required as with the former `@Value` parameters
 * (`reserved` is read by `ReservedPaths`). `treshold` keeps the key's historical spelling.
 */
@ConfigurationProperties("mt.link-spray.random-id")
data class RandomIdProperties(
    val length: Int,
    val prefix: String,
    val charset: String,
    val cache: Cache,
) {
    data class Cache(val enabled: Boolean, val depth: Int, val treshold: Int)
}

/** The random id generator with its settings, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RandomIdProperties::class)
class ShortenerConfig {

    @Bean
    fun randomIdGeneratorService(store: ShortLinkStore, reservedPaths: ReservedPathsPort, p: RandomIdProperties) =
        RandomIdGeneratorService(
            store, reservedPaths,
            RandomIdSettings(p.length, p.prefix, p.charset, p.cache.enabled, p.cache.depth, p.cache.treshold),
        )

    /** URLhaus check on every new short link (MLS-120); no key, no check (dev, tests, a key not yet set in prod). */
    @Bean
    fun urlReputation(@Value("\${mt.link-spray.urlhaus.auth-key:}") authKey: String): UrlReputationPort =
        if (authKey.isBlank()) UrlReputationPort { false } else UrlhausReputation(authKey)
}
