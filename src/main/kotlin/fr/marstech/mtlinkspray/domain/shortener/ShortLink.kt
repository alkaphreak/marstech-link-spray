package fr.marstech.mtlinkspray.domain.shortener
import java.time.LocalDateTime

/** A short link as the domain sees it: its id is the first path segment of the short URL. */
data class ShortLink(
    val id: String,
    val targetUrl: String,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val clickCount: Long = 0,
    val lastClickedAt: LocalDateTime? = null,
    /** Redirects requested by robots, kept out of [clickCount] (MLS-273). */
    val botClickCount: Long = 0,
)
