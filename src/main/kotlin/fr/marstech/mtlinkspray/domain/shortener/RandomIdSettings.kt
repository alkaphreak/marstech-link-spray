package fr.marstech.mtlinkspray.domain.shortener

/** `mt.link-spray.random-id.*`: generated id shape and the free-id cache (MLS-178). */
data class RandomIdSettings(
    val length: Int,
    val prefix: String,
    val charset: String,
    val cacheEnabled: Boolean,
    val cacheDepth: Int,
    val cacheThreshold: Int,
)
