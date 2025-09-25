package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.config.TestConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.text.MessageFormat
import java.util.stream.IntStream

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest
class RandomIdGeneratorServiceImplTest(
    val randomIdGeneratorService: RandomIdGeneratorServiceImpl,
) {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val mongoDBContainer =
            MongoDBContainer(DockerImageName.parse(TestConfig.MONGO_DB_DOCKER_IMAGE_NAME)).withReuse(true)
    }

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
