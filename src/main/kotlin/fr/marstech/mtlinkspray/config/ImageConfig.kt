package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.domain.image.ImageService
import fr.marstech.mtlinkspray.domain.image.ImageStoragePort
import fr.marstech.mtlinkspray.domain.image.ImageStore
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** `mt.link-spray.storage.max-file-size-bytes`; the `do-spaces.*` keys are read by `S3Config` and the S3 adapter. */
@ConfigurationProperties("mt.link-spray.storage")
data class StorageProperties(val maxFileSizeBytes: Long = 26_214_400)

/** Image storage service with its upload size limit, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(StorageProperties::class)
class ImageConfig {

    @Bean
    fun imageService(
        imageStore: ImageStore,
        apiKeyResolver: ApiKeyResolver,
        imageStorage: ImageStoragePort,
        p: StorageProperties,
    ): ImageService = ImageService(imageStore, apiKeyResolver, imageStorage, p.maxFileSizeBytes)
}
