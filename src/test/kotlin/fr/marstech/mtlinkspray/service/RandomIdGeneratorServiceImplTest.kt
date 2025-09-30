package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.repository.LinkItemRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.SpringBootTest
import java.text.MessageFormat
import java.util.stream.IntStream

@Disabled
@SpringBootTest
class RandomIdGeneratorServiceImplTest {
    private val linkItemRepository = mock(LinkItemRepository::class.java)
    private val randomIdGeneratorService = RandomIdGeneratorServiceImpl(linkItemRepository)

    @Test
    fun generateRandomId() {
        val randomId = randomIdGeneratorService.generateRandomId()
        Assertions.assertNotNull(randomId)
        Assertions.assertEquals(randomIdGeneratorService.length, randomId.length)
    }

    @Test
    fun benchmarkWithAndWithoutCache() {
        val testRange = 1
        val withCache: Long
        val withoutCache: Long

        // without cache
        var start: Long = System.currentTimeMillis()
        IntStream.range(0, testRange)
            .forEach { _: Int -> randomIdGeneratorService.getGeneratedFreeIdWithoutCache() }
        var end: Long = System.currentTimeMillis()
        withoutCache = end - start

        // with cache
        start = System.currentTimeMillis()
        IntStream.range(0, testRange)
            .forEach { _: Int -> randomIdGeneratorService.getGeneratedFreeIdWithCache() }
        end = System.currentTimeMillis()
        withCache = end - start

        // todo replace with logging
        println(
            MessageFormat.format(
                "With cache: {0}ms, Without cache: {1}ms, Difference: {2}ms, Test range: {3}, Cache depth: {4}, Cache treshold: {5}",
                withCache,
                withoutCache,
                withoutCache - withCache,
                testRange,
                randomIdGeneratorService.cacheDepth,
                randomIdGeneratorService.cacheTreshold
            )
        )
    }
}
