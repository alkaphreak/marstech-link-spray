package fr.marstech.mtlinkspray.service

import java.io.InputStream

/**
 * Abstraction over the object storage backend used for image storage (MLS-203).
 * Currently implemented against Digital Ocean Spaces (S3-compatible), but kept storage-agnostic
 * so a future migration to another S3-compatible backend (e.g. Garage, MinIO, RustFS - see
 * MARSTECH-550/599/544/623) would not impact callers.
 *
 * Images are served by proxying/streaming through MLS (no direct redirect to the storage
 * provider URL), hence [download] rather than a presigned-URL-only API.
 */
interface ImageStorageService {

    /**
     * Uploads [data] under the given [namespace] (per-app isolation) and returns the resulting
     * storage key.
     */
    fun upload(namespace: String, filename: String, contentType: String, data: ByteArray): String

    /**
     * Streams the binary content stored at [storageKey].
     */
    fun download(storageKey: String): InputStream

    /**
     * Deletes the object stored at [storageKey].
     */
    fun delete(storageKey: String)
}
