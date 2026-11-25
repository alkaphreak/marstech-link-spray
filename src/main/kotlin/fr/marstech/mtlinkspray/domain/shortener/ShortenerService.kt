package fr.marstech.mtlinkspray.domain.shortener

interface ShortenerService {

    /**
     * Get the shortened URL of the target URL
     *
     * @param url   the target URL
     * @param links builds the short URL from the new link's id
     * @return the shortened URL
     */
    fun shorten(url: String, links: LinkUrls): String?

    /**
     * Get the target URL from the shortened UID and count the click, as a robot's when the User-Agent says so
     *
     * @param uid       the shortened UID
     * @param userAgent the request's User-Agent, read in memory and never stored (MLS-273)
     * @return the target URL
     */
    fun getTarget(uid: String, userAgent: String?): String

    /** Click counts (people, robots) and last click date of a short link (MLS-261, MLS-273). */
    fun getStats(uid: String): LinkStats
}
