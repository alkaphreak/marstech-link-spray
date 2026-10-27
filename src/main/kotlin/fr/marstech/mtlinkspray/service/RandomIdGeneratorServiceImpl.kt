package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.objects.RandomIdGeneratorObject
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RandomIdGeneratorServiceImpl(
    private val linkItemRepository: LinkItemRepository,
    private val reservedPaths: ReservedPaths,
    @param:Value($$"${mt.link-spray.random-id.length}") val length: Int,
    @param:Value($$"${mt.link-spray.random-id.cache.enabled}") val isCacheEnabled: Boolean,
    @param:Value($$"${mt.link-spray.random-id.cache.depth}") val cacheDepth: Int,
    @param:Value($$"${mt.link-spray.random-id.cache.treshold}") val cacheTreshold: Int,
    @param:Value($$"${mt.link-spray.random-id.prefix}") private val prefix: String,
    @param:Value($$"${mt.link-spray.random-id.charset}") private val charset: String,
) : RandomIdGeneratorService {

    val cacheIds: LinkedHashSet<String> = LinkedHashSet()

    override fun getGeneratedFreeId(): String = when {
        isCacheEnabled -> getGeneratedFreeIdWithCache()
        else -> getGeneratedFreeIdWithoutCache()
    }

    override fun getGeneratedFreeIdWithoutCache(): String {
        var id: String
        var attempts = 0
        val maxAttempts = 5
        do {
            check(attempts++ < maxAttempts) { "Unable to generate a free ID after 5 attempts" }
            id = generateRandomId()
        } while (id in reservedPaths || linkItemRepository.existsById(id))
        return id
    }

    // Synchronized: two concurrent requests could otherwise take the same cached ID (MLS-178).
    @Synchronized
    override fun getGeneratedFreeIdWithCache(): String {
        if (cacheIds.size < cacheTreshold) {
            val linkItemIds = linkItemRepository.findAllIds().toSet()
            // Bounded: a nearly exhausted ID space must fail instead of looping forever.
            var rounds = 0
            while (cacheIds.size < cacheTreshold && rounds++ < MAX_REFILL_ROUNDS) {
                generateRandomIds(cacheDepth)
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
        prefix = prefix,
        totalLength = length,
        charset = charset.toCharArray()
    )

    companion object {
        private const val MAX_REFILL_ROUNDS = 5
    }
}