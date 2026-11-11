package fr.marstech.mtlinkspray.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * [ImageStorageService] implementation backed by Digital Ocean Spaces (S3-compatible object
 * storage). Bucket: mt-mls-img-storage (region tor1) - see MLS-203 tracking doc for provisioning
 * details.
 *
 * The client is absent when Spaces is not configured (MLS-263): every call then answers 503.
 */
@Service
class DigitalOceanSpacesServiceImpl(
    private val s3Client: S3Client?,
    @Value("\${mt.link-spray.storage.do-spaces.bucket}") private val bucket: String,
) : ImageStorageService {

    init {
        if (s3Client == null) log.warn("Image storage disabled: LINK_SPRAY_DO_SPACES_ENDPOINT is not set")
    }

    private fun client(): S3Client =
        s3Client ?: throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Image storage not configured")

    override fun upload(namespace: String, filename: String, contentType: String, data: ByteArray): String {
        val storageKey = buildStorageKey(namespace, filename)
        client().putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType(contentType)
                .contentLength(data.size.toLong())
                .build(),
            RequestBody.fromBytes(data)
        )
        return storageKey
    }

    override fun download(storageKey: String): InputStream =
        client().getObject(
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build()
        )

    override fun delete(storageKey: String) {
        client().deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build()
        )
    }

    /**
     * Builds the storage key as `{namespace}/{yyyy}/{MM}/{uuid}.{ext}` so that images are
     * isolated per app (namespace) from day one, per MLS-203 decisions.
     */
    private fun buildStorageKey(namespace: String, filename: String): String {
        val yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
        val extension = filename.substringAfterLast('.', missingDelimiterValue = "")
        val imageId = UUID.randomUUID().toString()
        val suffix = if (extension.isBlank()) imageId else "$imageId.$extension"
        return "$namespace/$yearMonth/$suffix"
    }

    companion object {
        private val log = LoggerFactory.getLogger(DigitalOceanSpacesServiceImpl::class.java)
    }
}
