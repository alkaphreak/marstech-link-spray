package fr.marstech.mtlinkspray.adapter.outbound.mongo.image

import fr.marstech.mtlinkspray.domain.image.Image
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ImageEntityTest {

    @Test
    fun shouldMapDomainImageBothWays() {
        val image = Image(
            id = "img-1",
            apiKeyId = "apikey-1",
            storageKey = "maeva-app-1/2026/07/img-1.jpg",
            originalFilename = "photo.jpg",
            contentType = "image/jpeg",
            sizeBytes = 1234,
            isPrivate = true,
            authorIpAddress = "203.0.113.7",
        )

        val entity = ImageEntity.of(image)

        assertEquals("maeva-app-1/2026/07/img-1.jpg", entity.s3Key)
        assertEquals("UPLOAD_IMAGE", entity.author.action)
        assertEquals("203.0.113.7", entity.author.ipAddress)
        assertEquals(image, entity.toDomain())
    }
}
