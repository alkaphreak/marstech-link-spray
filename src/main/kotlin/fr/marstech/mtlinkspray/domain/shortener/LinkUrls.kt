package fr.marstech.mtlinkspray.domain.shortener

/** Absolute URLs of short links and spray pages, built by the web adapter from the current request's address. */
interface LinkUrls {

    /** The short URL of the link [id]. */
    fun shortLink(id: String): String

    /** The spray page URL opening every link of [links]. */
    fun spray(links: List<String>): String
}
