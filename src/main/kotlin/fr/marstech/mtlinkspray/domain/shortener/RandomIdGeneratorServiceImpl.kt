package fr.marstech.mtlinkspray.domain.shortener

/** Built by `ShortenerConfig` from `mt.link-spray.random-id.*`. */
class RandomIdGeneratorServiceImpl(
    private val shortLinkStore: ShortLinkStore,
    private val reservedPaths: ReservedPathsPort,
    val settings: RandomIdSettings,
) : RandomIdGeneratorService {

    val cacheIds: LinkedHashSet<String> = LinkedHashSet()

    override fun getGeneratedFreeId(): String = when {
        settings.cacheEnabled -> getGeneratedFreeIdWithCache()
        else -> getGeneratedFreeIdWithoutCache()
    }

    override fun getGeneratedFreeIdWithoutCache(): String {
        var id: String
        var attempts = 0
        val maxAttempts = 5
        do {
            check(attempts++ < maxAttempts) { "Unable to generate a free ID after 5 attempts" }
            id = generateRandomId()
        } while (id in reservedPaths || shortLinkStore.existsById(id))
        return id
    }

    // Synchronized: two concurrent requests could otherwise take the same cached ID (MLS-178).
    @Synchronized
    override fun getGeneratedFreeIdWithCache(): String {
        if (cacheIds.size < settings.cacheThreshold) {
            val linkItemIds = shortLinkStore.findAllIds()
            // Bounded: a nearly exhausted ID space must fail instead of looping forever.
            var rounds = 0
            while (cacheIds.size < settings.cacheThreshold && rounds++ < MAX_REFILL_ROUNDS) {
                generateRandomIds(settings.cacheDepth)
                    .filterNot { it in linkItemIds || it in reservedPaths }
                    .forEach { cacheIds.add(it) }
            }
            check(cacheIds.isNotEmpty()) { "Unable to generate a free ID after $MAX_REFILL_ROUNDS cache refills" }
        }
        return cacheIds.first().also { cacheIds.remove(it) }
    }

    fun generateRandomIds(count: Int): Set<String> = (1..count)
        .mapTo(mutableSetOf()) { generateRandomId() }

    override fun generateRandomId(): String = RandomIdGeneratorObject.generate(
        prefix = settings.prefix,
        totalLength = settings.length,
        charset = settings.charset.toCharArray()
    )

    companion object {
        private const val MAX_REFILL_ROUNDS = 5
    }
}