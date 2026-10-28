package fr.marstech.mtlinkspray.service

import java.io.InputStream

interface MarkdownToPdfService {
    /**
     * Renders the Markdown read from [markdown] to a PDF, on behalf of the app identified by
     * [rawApiKey] (MLS-223). The key is checked before the body is read. Throws
     * [IllegalAccessException] if the API key is invalid, [IllegalArgumentException] if the
     * Markdown is blank, and [fr.marstech.mtlinkspray.exception.PayloadTooLargeException] if it
     * exceeds the configured maximum size.
     */
    fun render(rawApiKey: String?, markdown: InputStream): ByteArray
}
