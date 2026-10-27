package fr.marstech.mtlinkspray.service

import fr.marstech.mtlinkspray.repository.LinkItemRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.text.MessageFormat
import java.util.concurrent.Executors
import java.util.stream.IntStream

@SpringBootTest
class RandomIdGeneratorServiceImplTest {

    @MockitoBean
    private lateinit var linkItemRepository: LinkItemRepository

    @Autowired
    private lateinit var randomIdGeneratorService: RandomIdGeneratorServiceImpl

    @BeforeEach
    fun setUp() {
        reset(linkItemRepository)
        randomIdGeneratorService.cacheIds.clear()
    }

    @Test
    fun generateRandomIdShouldReturnNonNullValue() {
        // When
        val id = randomIdGeneratorService.generateRandomId()

        // Then
        assertNotNull(id)
    }

    @Test
    fun generateRandomIdShouldReturnIdWithConfiguredLength() {
        // When
        val id = randomIdGeneratorService.generateRandomId()

        // Then
        assertEquals(randomIdGeneratorService.length, id.length)
    }

    @Test
    fun generateRandomIdsShouldReturnRequestedCount() {
        // Given
        val count = 10

        // When
        val ids = randomIdGeneratorService.generateRandomIds(count)

        // Then
        assertEquals(count, ids.size)
    }

    @Test
    fun generateRandomIdsShouldReturnUniqueIds() {
        // Given
        val count = 20

        // When
        val ids = randomIdGeneratorService.generateRandomIds(count)

        // Then
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun generateRandomIdsShouldReturnIdsWithCorrectLength() {
        // When
        val ids = randomIdGeneratorService.generateRandomIds(5)

        // Then
        ids.forEach { assertEquals(randomIdGeneratorService.length, it.length) }
    }

    @Test
    fun getGeneratedFreeIdShouldDelegateToWithCacheWhenCacheEnabled() {
        // Given
        whenever(linkItemRepository.findAllIds()).thenReturn(emptyList())

        // When
        val id = randomIdGeneratorService.getGeneratedFreeId()

        // Then
        assertNotNull(id)
        assertEquals(randomIdGeneratorService.length, id.length)
    }

    @Test
    fun getGeneratedFreeIdWithoutCacheShouldReturnIdOnFirstAttempt() {
        // Given
        whenever(linkItemRepository.existsById(anyOrNull())).thenReturn(false)

        // When
        val id = randomIdGeneratorService.getGeneratedFreeIdWithoutCache()

        // Then
        assertNotNull(id)
        assertEquals(randomIdGeneratorService.length, id.length)
    }

    @Test
    fun getGeneratedFreeIdWithoutCacheShouldRetryWhenIdAlreadyExists() {
        // Given — first 3 generated ids are taken, the 4th is free
        whenever(linkItemRepository.existsById(anyOrNull()))
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false)

        // When
        val id = randomIdGeneratorService.getGeneratedFreeIdWithoutCache()

        // Then
        assertNotNull(id)
        verify(linkItemRepository, times(4)).existsById(anyOrNull())
    }

    @Test
    fun getGeneratedFreeIdWithoutCacheShouldThrowWhenMaxAttemptsExceeded() {
        // Given — all attempts return true (id always taken)
        whenever(linkItemRepository.existsById(anyOrNull())).thenReturn(true)

        // When / Then
        assertThrows(IllegalStateException::class.java) {
            randomIdGeneratorService.getGeneratedFreeIdWithoutCache()
        }
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldReturnIdAndRemoveItFromCache() {
        // Given
        whenever(linkItemRepository.findAllIds()).thenReturn(emptyList())

        // When
        val id = randomIdGeneratorService.getGeneratedFreeIdWithCache()

        // Then
        assertNotNull(id)
        assertEquals(randomIdGeneratorService.length, id.length)
        assertFalse(randomIdGeneratorService.cacheIds.contains(id))
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldRefillCacheWhenBelowThreshold() {
        // Given — cache is empty, the repository has no existing ids
        whenever(linkItemRepository.findAllIds()).thenReturn(emptyList())

        // When
        randomIdGeneratorService.getGeneratedFreeIdWithCache()

        // Then — repository was consulted to build the cache
        verify(linkItemRepository, atLeastOnce()).findAllIds()
        assertTrue(randomIdGeneratorService.cacheIds.size >= randomIdGeneratorService.cacheTreshold - 1)
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldNotRefillCacheWhenAboveThreshold() {
        // Given — pre-fill cache above a threshold, so no refill is needed
        val preFilledIds = (1..randomIdGeneratorService.cacheTreshold + 5)
            .mapTo(LinkedHashSet()) { randomIdGeneratorService.generateRandomId() }
        randomIdGeneratorService.cacheIds.addAll(preFilledIds)

        // When
        randomIdGeneratorService.getGeneratedFreeIdWithCache()

        // Then — repository should not have been called
        verify(linkItemRepository, never()).findAllIds()
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldExcludeExistingRepositoryIds() {
        // Given — cache is empty (below threshold), repository reports some ids as already taken
        val existingId = randomIdGeneratorService.generateRandomId()
        whenever(linkItemRepository.findAllIds()).thenReturn(listOf(existingId))

        // When — cache refill is triggered, existing ids must be filtered out
        val id = randomIdGeneratorService.getGeneratedFreeIdWithCache()

        // Then — the returned id was not among the repository's existing ids
        assertNotEquals(existingId, id)
        assertFalse(randomIdGeneratorService.cacheIds.contains(existingId))
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldDecreaseCacheSizeByOne() {
        // Given — pre-fill cache above threshold
        repeat(randomIdGeneratorService.cacheTreshold + 5) {
            randomIdGeneratorService.cacheIds.add(randomIdGeneratorService.generateRandomId())
        }
        val cacheSizeBefore = randomIdGeneratorService.cacheIds.size

        // When
        randomIdGeneratorService.getGeneratedFreeIdWithCache()

        // Then
        assertEquals(cacheSizeBefore - 1, randomIdGeneratorService.cacheIds.size)
    }

    @Autowired
    private lateinit var reservedPaths: ReservedPaths

    private val tinyRepository = mock<LinkItemRepository>()
    private val tinyReservedPaths = mock<ReservedPaths>()

    private fun givenTinyIdSpaceService(
        charset: String,
        takenIds: List<String>,
        isCacheEnabled: Boolean = true,
    ): RandomIdGeneratorServiceImpl {
        whenever(tinyRepository.findAllIds()).thenReturn(takenIds)
        return RandomIdGeneratorServiceImpl(tinyRepository, tinyReservedPaths, 1, isCacheEnabled, 10, 3, "", charset)
    }

    @Test
    fun reservedPathsShouldContainControllerRoutesAndConfiguredPaths() {
        // Then — first segments of the controller mappings, plus the configured non-controller paths
        listOf("api", "spray", "paste", "qr", "random", "uuid", "dashboard", "abuse", "shortener", "admin", "index")
            .forEach { assertTrue(it in reservedPaths, "$it should be reserved") }
        listOf("assets", "actuator", "error", "favicon.ico")
            .forEach { assertTrue(it in reservedPaths, "$it should be reserved") }
        assertFalse(reservedPaths.segments.any { it.startsWith("{") })
        assertFalse("abcde" in reservedPaths)
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldSkipReservedPaths() {
        // Given — "a" is a route, only "b" can be served
        whenever(tinyReservedPaths.contains("a")).thenReturn(true)
        val service = givenTinyIdSpaceService("ab", emptyList())

        // When / Then
        assertEquals("b", service.getGeneratedFreeIdWithCache())
    }

    @Test
    fun getGeneratedFreeIdWithoutCacheShouldSkipReservedPaths() {
        // Given — the only possible id is a route
        whenever(tinyReservedPaths.contains("a")).thenReturn(true)
        val service = givenTinyIdSpaceService("a", emptyList(), isCacheEnabled = false)

        // When / Then — retries exhaust instead of returning the route
        assertThrows(IllegalStateException::class.java) { service.getGeneratedFreeIdWithoutCache() }
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldThrowWhenIdSpaceIsExhausted() {
        // Given — the only possible id is already taken
        val service = givenTinyIdSpaceService("a", listOf("a"))

        // When / Then — the refill gives up instead of looping forever
        assertThrows(IllegalStateException::class.java) { service.getGeneratedFreeIdWithCache() }
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldReturnRemainingIdsWhenThresholdIsUnreachable() {
        // Given — only "b" is free, below the cache threshold of 3
        val service = givenTinyIdSpaceService("ab", listOf("a"))

        // When
        val id = service.getGeneratedFreeIdWithCache()

        // Then — the last free id is served; once it is saved, the next call fails
        assertEquals("b", id)
        whenever(tinyRepository.findAllIds()).thenReturn(listOf("a", "b"))
        assertThrows(IllegalStateException::class.java) { service.getGeneratedFreeIdWithCache() }
    }

    @Test
    fun getGeneratedFreeIdWithCacheShouldNeverReturnTheSameIdToConcurrentCallers() {
        // Given
        whenever(linkItemRepository.findAllIds()).thenReturn(emptyList())
        val pool = Executors.newFixedThreadPool(8)

        // When
        val ids = try {
            (1..2000).map { pool.submit<String> { randomIdGeneratorService.getGeneratedFreeIdWithCache() } }
                .map { it.get() }
        } finally {
            pool.shutdown()
        }

        // Then
        assertEquals(ids.size, ids.toSet().size)
    }

    @EnabledIfSystemProperty(named = "spring.profiles.active", matches = ".*benchmark.*")
    @Test
    fun benchmarkWithAndWithoutCache() {
        val testRange = 10000
        val withCache: Long
        val withoutCache: Long

        var start: Long = System.currentTimeMillis()
        IntStream.range(0, testRange)
            .forEach { _: Int -> randomIdGeneratorService.getGeneratedFreeIdWithoutCache() }
        var end: Long = System.currentTimeMillis()
        withoutCache = end - start

        start = System.currentTimeMillis()
        IntStream.range(0, testRange)
            .forEach { _: Int -> randomIdGeneratorService.getGeneratedFreeIdWithCache() }
        end = System.currentTimeMillis()
        withCache = end - start

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
