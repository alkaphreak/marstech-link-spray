package fr.marstech.mtlinkspray.config

import fr.marstech.mtlinkspray.domain.image.ImageService
import fr.marstech.mtlinkspray.domain.image.ImageServiceImpl
import fr.marstech.mtlinkspray.domain.image.ImageStorageService
import fr.marstech.mtlinkspray.domain.image.ImageStore
import fr.marstech.mtlinkspray.domain.shared.ApiKeyResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Image storage service with its upload size limit, so the domain reads no Spring property (ADR 0007). */
@Configuration(proxyBeanMethods = false)
class ImageConfig {

    @Bean
    fun imageService(
        imageStore: ImageStore,
        apiKeyResolver: ApiKeyResolver,
        imageStorageService: ImageStorageService,
        @Value("\${mt.link-spray.storage.max-file-size-bytes:26214400}") maxFileSizeBytes: Long,
    ): ImageService = ImageServiceImpl(imageStore, apiKeyResolver, imageStorageService, maxFileSizeBytes)
}
