package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.adapter.outbound.mongo.dashboard.DashboardLinkEntity
import org.springframework.boot.persistence.autoconfigure.EntityScanner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoManagedTypes
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Boot's entity scan (every `@Document`), plus the concrete types stored under a polymorphic field,
 * so their `@TypeAlias` resolves on a cold start (ADR 0007): `DashboardEntity.items` holds
 * [DashboardLinkEntity] values, which are not documents, and documents stored before the package move
 * carry the old class name in `_class`.
 */
@Configuration(proxyBeanMethods = false)
class MongoConfig {

    @Bean
    fun mongoManagedTypes(context: ApplicationContext): MongoManagedTypes =
        MongoManagedTypes.fromIterable(EntityScanner(context).scan(Document::class.java) + DashboardLinkEntity::class.java)
}
