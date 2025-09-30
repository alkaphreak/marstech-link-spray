package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.objects.RandomIdGeneratorObject
import fr.marstech.mtlinkspray.repository.LinkItemRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RandomIdGeneratorServiceImpl(
    val linkItemRepository: LinkItemRepository
) : RandomIdGeneratorService {

    @Value($$"${mt.link-spray.random-id.length}")
    var length: Int = 0

    @Value($$"${mt.link-spray.random-id.cache.enabled}")
    val isCacheEnabled: Boolean = true

    @Value($$"${mt.link-spray.random-id.cache.depth}")
    val cacheDepth: Int = 100

    @JvmField
    @Value($$"${mt.link-spray.random-id.cache.treshold}")
    val cacheTreshold: Int = 10

    @Value($$"${mt.link-spray.random-id.prefix}")
    val prefix: String = ""

    @Value($$"${mt.link-spray.random-id.charset}")
    val charset: String = (0..127).map(Int::toChar).joinToString("")

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
            if (attempts++ >= maxAttempts) throw IllegalStateException("Unable to generate a free ID after 5 attempts")
            id = generate()
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
        return cacheIds.removeFirst()
    }

    fun generateRandomIds(count: Int): Set<String> = (1..count).mapTo(mutableSetOf()) { generate() }

    override fun generateRandomId(): String = generate()

    private fun generate(): String = RandomIdGeneratorObject.generate(prefix, length, charset)
}
