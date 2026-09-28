package fr.marstech.mtlinkspray.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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
 */
@Service
class DigitalOceanSpacesServiceImpl(
    private val s3Client: S3Client,
    @Value("\${mt.link-spray.storage.do-spaces.bucket}") private val bucket: String,
) : ImageStorageService {

    override fun upload(namespace: String, filename: String, contentType: String, data: ByteArray): String {
        val storageKey = buildStorageKey(namespace, filename)
        s3Client.putObject(
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
        s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build()
        )

    override fun delete(storageKey: String) {
        s3Client.deleteObject(
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
}
