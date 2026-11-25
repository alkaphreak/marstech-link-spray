package fr.marstech.mtlinkspray.domain.shortener
import java.time.LocalDateTime

/** Short link storage, implemented by the Mongo adapter. */
interface ShortLinkStore {

    /**
     * Creates a link to [targetUrl] under an id drawn from [newId], never overwriting an existing one: when the
     * id is already taken, a new one is drawn, up to [attempts] tries, and the last failure propagates (MLS-246).
     */
    fun createWithFreeId(targetUrl: String, attempts: Int, newId: () -> String): ShortLink

    fun findById(id: String): ShortLink?

    fun existsById(id: String): Boolean

    fun findAllIds(): Set<String>

    /** Atomic: concurrent clicks are never lost (MLS-261). */
    fun countClick(id: String, clickedAt: LocalDateTime)

    /** Robots only bump their own counter, never the last-click date (MLS-273). */
    fun countBotClick(id: String)
}
