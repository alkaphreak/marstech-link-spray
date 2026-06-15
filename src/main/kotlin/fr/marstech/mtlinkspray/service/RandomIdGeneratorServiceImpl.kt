package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.objects.RandomIdGeneratorObject
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RandomIdGeneratorServiceImpl(
    private val linkItemRepository: LinkItemRepository,
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
        } while (linkItemRepository.existsById(id))
        return id
    }

    override fun getGeneratedFreeIdWithCache(): String {
        if (cacheIds.size < cacheTreshold) {
            val linkItemIds = linkItemRepository.findAllIds().toSet()
            while (cacheIds.size < cacheTreshold) {
                generateRandomIds(cacheDepth)
                    .filterNot { it in linkItemIds }
                    .forEach { cacheIds.add(it) }
            }
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
}