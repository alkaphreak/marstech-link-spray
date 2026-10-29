package fr.marstech.mtlinkspray.service

import java.io.InputStream

interface PdfToMarkdownService {
    /**
     * Extracts the text of the PDF read from [pdf] as Markdown, on behalf of the app identified by
     * [rawApiKey] (MLS-196). The key is checked before the body is read. Throws
     * [IllegalAccessException] if the API key is invalid, [IllegalArgumentException] if the body
     * is empty, not a PDF or encrypted, and [fr.marstech.mtlinkspray.exception.PayloadTooLargeException]
     * if it exceeds the configured maximum size or page count.
     */
    fun convert(rawApiKey: String?, pdf: InputStream): String
}
