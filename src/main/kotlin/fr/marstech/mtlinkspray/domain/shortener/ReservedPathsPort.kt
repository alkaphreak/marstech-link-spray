package fr.marstech.mtlinkspray.domain.shortener

/** First path segments a generated id must never take, since a route would shadow the short link (MLS-179). */
fun interface ReservedPathsPort {

    operator fun contains(id: String): Boolean
}
