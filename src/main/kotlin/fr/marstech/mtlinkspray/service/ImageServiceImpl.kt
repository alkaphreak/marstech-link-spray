package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.entity.HistoryItem
import fr.marstech.mtlinkspray.entity.ImageEntity
import fr.marstech.mtlinkspray.repository.ImageRepository
import fr.marstech.mtlinkspray.utils.ImageTypes
import fr.marstech.mtlinkspray.utils.NetworkUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private val apiKeyService: ApiKeyService,
    private val imageStorageService: ImageStorageService,
    @Value("\${mt.link-spray.storage.max-file-size-bytes:26214400}") private val maxFileSizeBytes: Long,
) : ImageService {

    override fun uploadImage(
        rawApiKey: String?,
        file: MultipartFile,
        isPrivate: Boolean,
        httpServletRequest: HttpServletRequest,
    ): ImageEntity {
        val apiKey = apiKeyService.resolve(rawApiKey)

        if (file.isEmpty) throw IllegalArgumentException("Uploaded file is empty")
        if (file.size > maxFileSizeBytes) {
            throw IllegalArgumentException(
                "File size ${file.size} bytes exceeds the maximum allowed size of $maxFileSizeBytes bytes"
            )
        }

        val filename = file.originalFilename?.takeIf { it.isNotBlank() } ?: "upload"
        val bytes = file.bytes
        val contentType = ImageTypes.detect(bytes) ?: throw IllegalArgumentException(
            "Unsupported file type: only ${ImageTypes.ALLOWED.joinToString()} are accepted"
        )
        val storageKey = imageStorageService.upload(apiKey.namespace, filename, contentType, bytes)

        return imageRepository.save(
            ImageEntity(
                apiKeyId = apiKey.id,
                s3Key = storageKey,
                originalFilename = filename,
                contentType = contentType,
                sizeBytes = file.size,
                isPrivate = isPrivate,
                author = HistoryItem(
                    ipAddress = NetworkUtils.getIpAddress(httpServletRequest),
                    action = "UPLOAD_IMAGE"
                ),
            )
        )
    }

    override fun getImage(id: String, rawApiKey: String?): ImageEntity =
        findImage(id).also { checkAccess(it, rawApiKey) }

    override fun downloadImage(id: String, rawApiKey: String?): Pair<ImageEntity, InputStream> {
        val image = getImage(id, rawApiKey)
        return image to imageStorageService.download(image.s3Key)
    }

    override fun deleteImage(id: String, rawApiKey: String?) {
        val apiKey = apiKeyService.resolve(rawApiKey)
        val image = findImage(id)
        if (image.apiKeyId != apiKey.id) throw IllegalAccessException("Not authorized to delete this image")

        imageStorageService.delete(image.s3Key)
        imageRepository.deleteById(id)
    }

    override fun listImages(rawApiKey: String?): List<ImageEntity> {
        val apiKey = apiKeyService.resolve(rawApiKey)
        return imageRepository.findByApiKeyId(apiKey.id)
    }

    private fun findImage(id: String): ImageEntity =
        imageRepository.findById(id).orElseThrow { NoSuchElementException("Image $id not found") }

    /**
     * Public images are readable by anyone; private images require a valid API key matching
     * the owning app.
     */
    private fun checkAccess(image: ImageEntity, rawApiKey: String?) {
        if (!image.isPrivate) return
        val apiKey = apiKeyService.resolve(rawApiKey)
        if (image.apiKeyId != apiKey.id) throw IllegalAccessException("Not authorized to access this image")
    }
}
